package io.beanthemoonman.pokeapp.uistate.moves

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.beanthemoonman.pokeapp.domain.model.Generation
import io.beanthemoonman.pokeapp.domain.model.MoveCategory
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.usecase.GetMovePageUseCase
import io.beanthemoonman.pokeapp.domain.usecase.GetMovePageUseCase.Companion.PAGE_SIZE
import io.beanthemoonman.pokeapp.domain.usecase.ObserveSelectedGenerationUseCase
import io.beanthemoonman.pokeapp.domain.usecase.SearchMovesUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class MovesListViewModel @Inject constructor(
  private val getMovePage: GetMovePageUseCase,
  private val searchMoves: SearchMovesUseCase,
  observeSelectedGeneration: ObserveSelectedGenerationUseCase,
) : ViewModel() {

  /** Active generation — drives both the header chrome AND which moves are in scope. */
  val generation: StateFlow<Generation?> = observeSelectedGeneration()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  /** Generation id backing every page/search request; null until one is selected. */
  private val genId = MutableStateFlow<Int?>(null)

  private val _state = MutableStateFlow<UiState<MovesListData>>(UiState.Loading)
  val state: StateFlow<UiState<MovesListData>> = _state.asStateFlow()

  private val _query = MutableStateFlow("")
  val query: StateFlow<String> = _query.asStateFlow()

  /** Bumped to re-run the current search after a failure (query unchanged). */
  private val _searchRetry = MutableStateFlow(0)

  val searchState: StateFlow<MovesSearchUiState> =
    combine(
      _query.debounce { if (it.isBlank()) 0L else SEARCH_DEBOUNCE_MS }.map { it.trim() },
      _searchRetry,
      genId,
    ) { q, retry, gen -> Triple(q, retry, gen) }
      .distinctUntilChanged()
      .flatMapLatest { (q, _, gen) ->
        if (q.isEmpty() || gen == null) {
          flowOf(MovesSearchUiState.Idle)
        } else {
          flow {
            Timber.i("move search gen=%d q=%s", gen, q)
            emit(MovesSearchUiState.Loading)
            val results = searchMoves(gen, q)
            Timber.d("move search gen=%d q=%s results=%d", gen, q, results.size)
            emit(
              if (results.isEmpty()) MovesSearchUiState.Empty else MovesSearchUiState.Results(
                results
              )
            )
          }.catch { e ->
            Timber.e(e, "move search failed q=%s", q)
            emit(MovesSearchUiState.Error(e.message ?: "Search failed"))
          }
        }
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MovesSearchUiState.Idle)

  private var nextOffset = 0
  private var pagingJob: Job? = null

  init {
    // Reload the dictionary whenever the selected generation changes.
    viewModelScope.launch {
      generation.collect { gen ->
        val id = gen?.id ?: return@collect
        if (genId.value != id) {
          genId.value = id
          Timber.i("moves generation -> %d, reloading dictionary", id)
          loadFirstPage(id)
        }
      }
    }
  }

  fun onQueryChange(value: String) {
    _query.value = value
  }

  fun clearSearch() {
    Timber.d("move search cleared")
    _query.value = ""
  }

  fun retrySearch() {
    Timber.i("move search retry requested q=%s", _query.value)
    _searchRetry.value += 1
  }

  /** Selects (or clears, when null) the active damage-class filter. */
  fun selectCategory(category: MoveCategory?) {
    val current = _state.value
    if (current is UiState.Success) {
      Timber.d("move class -> %s", category?.name ?: "all")
      _state.value = UiState.Success(current.data.copy(category = category))
    }
  }

  private fun loadFirstPage(generationId: Int) {
    nextOffset = 0
    _state.value = UiState.Loading
    pagingJob?.cancel()
    pagingJob = viewModelScope.launch {
      try {
        Timber.d(
          "moves loadFirstPage gen=%d offset=%d size=%d",
          generationId,
          nextOffset,
          PAGE_SIZE
        )
        val page = getMovePage(generationId, nextOffset, PAGE_SIZE)
        ensureActive()
        nextOffset += page.size
        val end = page.size < PAGE_SIZE
        Timber.d(
          "moves loadFirstPage done got=%d nextOffset=%d endReached=%b",
          page.size,
          nextOffset,
          end
        )
        _state.value = UiState.Success(MovesListData(moves = page, endReached = end))
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Timber.e(e, "moves loadFirstPage failed gen=%d offset=%d", generationId, nextOffset)
        _state.value = UiState.Error(e.message ?: "Something went wrong")
      }
    }
  }

  fun loadMore() {
    val current = _state.value
    if (current !is UiState.Success) return
    val gen = genId.value ?: return
    val data = current.data
    if (data.isAppending || data.endReached) {
      Timber.d(
        "moves loadMore skipped: isAppending=%b endReached=%b",
        data.isAppending,
        data.endReached
      )
      return
    }
    pagingJob?.cancel()
    pagingJob = viewModelScope.launch {
      _state.value = UiState.Success(data.copy(isAppending = true, appendError = false))
      try {
        Timber.d(
          "moves loadMore gen=%d offset=%d size=%d (have=%d)",
          gen,
          nextOffset,
          PAGE_SIZE,
          data.moves.size
        )
        val page = getMovePage(gen, nextOffset, PAGE_SIZE)
        ensureActive()
        nextOffset += page.size
        val end = page.size < PAGE_SIZE
        Timber.d(
          "moves loadMore done got=%d total=%d nextOffset=%d endReached=%b",
          page.size,
          data.moves.size + page.size,
          nextOffset,
          end
        )
        _state.value = UiState.Success(
          data.copy(
            moves = data.moves + page,
            isAppending = false,
            endReached = end
          )
        )
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Timber.e(e, "moves loadMore failed gen=%d offset=%d", gen, nextOffset)
        _state.value = UiState.Success(data.copy(isAppending = false, appendError = true))
      }
    }
  }

  /** Retry the first page (error state) or the failed append (success tail). */
  fun retry() {
    if (_state.value is UiState.Error) {
      genId.value?.let { loadFirstPage(it) }
    } else {
      loadMore()
    }
  }

  private companion object {
    const val SEARCH_DEBOUNCE_MS = 300L
  }
}
