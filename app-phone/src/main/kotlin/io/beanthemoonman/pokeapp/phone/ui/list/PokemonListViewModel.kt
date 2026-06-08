package io.beanthemoonman.pokeapp.phone.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.beanthemoonman.pokeapp.domain.model.Generation
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.usecase.GetPokemonPageUseCase
import io.beanthemoonman.pokeapp.domain.usecase.GetPokemonPageUseCase.Companion.PAGE_SIZE
import io.beanthemoonman.pokeapp.domain.usecase.ObserveSelectedGenerationUseCase
import io.beanthemoonman.pokeapp.domain.usecase.SearchPokemonUseCase
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val getPokemonPage: GetPokemonPageUseCase,
    private val searchPokemon: SearchPokemonUseCase,
    observeSelectedGeneration: ObserveSelectedGenerationUseCase,
) : ViewModel() {

    /** Active generation context (drives the header + which dex is paged). */
    val generation: StateFlow<Generation?> = observeSelectedGeneration()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _state = MutableStateFlow<UiState<PokemonListData>>(UiState.Loading)
    val state: StateFlow<UiState<PokemonListData>> = _state.asStateFlow()

    /** Raw search box text. Blank means the paged list is shown instead of results. */
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /**
     * Bumped to re-run the current search after a failure. Folded into the search flow so
     * a retry fires even when neither the query nor the generation changed.
     */
    private val _searchRetry = MutableStateFlow(0)

    /**
     * Search results, recomputed whenever the (debounced) query or selected generation
     * changes. A blank query is [SearchUiState.Idle]; `flatMapLatest` cancels an in-flight
     * search when the user keeps typing or switches generation.
     */
    val searchState: StateFlow<SearchUiState> =
        combine(
            _query.debounce { if (it.isBlank()) 0L else SEARCH_DEBOUNCE_MS }.map { it.trim() },
            generation.filterNotNull(),
            _searchRetry,
        ) { q, gen, retry -> Triple(q, gen, retry) }
            .distinctUntilChanged()
            .flatMapLatest { (q, gen, _) ->
                if (q.isEmpty()) {
                    flowOf(SearchUiState.Idle)
                } else {
                    flow {
                        Timber.i("search q=%s gen=%d dexEnd=%d", q, gen.id, gen.dexEnd)
                        emit(SearchUiState.Loading)
                        val results = searchPokemon(q, gen.dexEnd)
                        Timber.d("search q=%s results=%d", q, results.size)
                        emit(if (results.isEmpty()) SearchUiState.Empty else SearchUiState.Results(results))
                    }.catch { e ->
                        Timber.e(e, "search failed q=%s", q)
                        emit(SearchUiState.Error(e.message ?: "Search failed"))
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState.Idle)

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun clearSearch() {
        Timber.d("search cleared")
        _query.value = ""
    }

    /** Re-run the current search after a failure (query/generation unchanged). */
    fun retrySearch() {
        Timber.i("search retry requested q=%s", _query.value)
        _searchRetry.value += 1
    }

    private var activeGeneration: Generation? = null
    private var nextStartId = 1
    private var pagingJob: Job? = null

    init {
        // Restart paging whenever the selected generation changes.
        generation.filterNotNull()
            .map { it.id }
            .distinctUntilChanged()
            .onEach { id ->
                Timber.i("generation changed -> id=%d; restarting paging", id)
                restartForGeneration()
            }
            .launchIn(viewModelScope)
    }

    private fun restartForGeneration() {
        activeGeneration = generation.value
        nextStartId = 1
        Timber.d("restartForGeneration gen=%s nextStartId reset to %d", activeGeneration?.id, nextStartId)
        _state.value = UiState.Loading
        loadFirstPage()
    }

    private fun loadFirstPage() {
        val gen = activeGeneration ?: run {
            Timber.w("loadFirstPage skipped: no active generation")
            return
        }
        pagingJob?.cancel()
        pagingJob = viewModelScope.launch {
            try {
                Timber.d("loadFirstPage fetching gen=%d start=%d size=%d", gen.id, nextStartId, PAGE_SIZE)
                val page = getPokemonPage(nextStartId, PAGE_SIZE, gen.dexEnd)
                ensureActive() // a generation switch cancels us; don't commit stale results
                nextStartId += page.size
                val end = isEnd(page.size, gen)
                Timber.d("loadFirstPage done gen=%d got=%d nextStartId=%d endReached=%b", gen.id, page.size, nextStartId, end)
                _state.value = UiState.Success(PokemonListData(items = page, endReached = end))
            } catch (e: CancellationException) {
                Timber.d("loadFirstPage cancelled (gen switch) gen=%d", gen.id)
                throw e // never let a cancelled load write state
            } catch (e: Exception) {
                Timber.e(e, "loadFirstPage failed gen=%d start=%d", gen.id, nextStartId)
                _state.value = UiState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    fun loadMore() {
        val gen = activeGeneration ?: run {
            Timber.w("loadMore skipped: no active generation")
            return
        }
        val current = _state.value
        if (current !is UiState.Success) {
            Timber.d("loadMore skipped: state is %s (not Success)", current::class.simpleName)
            return
        }
        val data = current.data
        if (data.isAppending || data.endReached) {
            Timber.d("loadMore skipped: isAppending=%b endReached=%b items=%d", data.isAppending, data.endReached, data.items.size)
            return
        }

        pagingJob?.cancel()
        pagingJob = viewModelScope.launch {
            _state.value = UiState.Success(data.copy(isAppending = true, appendError = false))
            try {
                Timber.d("loadMore fetching gen=%d start=%d size=%d (have=%d)", gen.id, nextStartId, PAGE_SIZE, data.items.size)
                val page = getPokemonPage(nextStartId, PAGE_SIZE, gen.dexEnd)
                ensureActive() // a generation switch cancels us; don't commit stale results
                nextStartId += page.size
                val end = isEnd(page.size, gen)
                Timber.d("loadMore done gen=%d got=%d total=%d nextStartId=%d endReached=%b", gen.id, page.size, data.items.size + page.size, nextStartId, end)
                _state.value = UiState.Success(PokemonListData(items = data.items + page, endReached = end))
            } catch (e: CancellationException) {
                Timber.d("loadMore cancelled (gen switch) gen=%d start=%d", gen.id, nextStartId)
                throw e // never let a cancelled append write appendError onto stale data
            } catch (e: Exception) {
                Timber.e(e, "loadMore failed gen=%d start=%d", gen.id, nextStartId)
                _state.value = UiState.Success(data.copy(isAppending = false, appendError = true))
            }
        }
    }

    /** Retry the first page (error state) or the failed append (success tail). */
    fun retry() {
        Timber.i("retry requested; state=%s", _state.value::class.simpleName)
        if (_state.value is UiState.Error) loadFirstPage() else loadMore()
    }

    private fun isEnd(pageSize: Int, gen: Generation): Boolean =
        pageSize < PAGE_SIZE || nextStartId > gen.dexEnd

    private companion object {
        /** Wait this long after the last keystroke before firing a search. */
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
