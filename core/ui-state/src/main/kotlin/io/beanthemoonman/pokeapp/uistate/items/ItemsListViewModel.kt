package io.beanthemoonman.pokeapp.uistate.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.beanthemoonman.pokeapp.domain.model.Generation
import io.beanthemoonman.pokeapp.domain.model.ItemCategory
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.usecase.GetItemPageUseCase
import io.beanthemoonman.pokeapp.domain.usecase.GetItemPageUseCase.Companion.PAGE_SIZE
import io.beanthemoonman.pokeapp.domain.usecase.ObserveSelectedGenerationUseCase
import io.beanthemoonman.pokeapp.domain.usecase.SearchItemsUseCase
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ItemsListViewModel @Inject constructor(
    private val getItemPage: GetItemPageUseCase,
    private val searchItems: SearchItemsUseCase,
    observeSelectedGeneration: ObserveSelectedGenerationUseCase,
) : ViewModel() {

    /** Active generation — drives the header chrome only; items are national. */
    val generation: StateFlow<Generation?> = observeSelectedGeneration()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _state = MutableStateFlow<UiState<ItemsListData>>(UiState.Loading)
    val state: StateFlow<UiState<ItemsListData>> = _state.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** Bumped to re-run the current search after a failure (query unchanged). */
    private val _searchRetry = MutableStateFlow(0)

    val searchState: StateFlow<ItemsSearchUiState> =
        combine(
            _query.debounce { if (it.isBlank()) 0L else SEARCH_DEBOUNCE_MS }.map { it.trim() },
            _searchRetry,
        ) { q, retry -> q to retry }
            .distinctUntilChanged()
            .flatMapLatest { (q, _) ->
                if (q.isEmpty()) {
                    flowOf(ItemsSearchUiState.Idle)
                } else {
                    flow {
                        Timber.i("item search q=%s", q)
                        emit(ItemsSearchUiState.Loading)
                        val results = searchItems(q)
                        Timber.d("item search q=%s results=%d", q, results.size)
                        emit(if (results.isEmpty()) ItemsSearchUiState.Empty else ItemsSearchUiState.Results(results))
                    }.catch { e ->
                        Timber.e(e, "item search failed q=%s", q)
                        emit(ItemsSearchUiState.Error(e.message ?: "Search failed"))
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ItemsSearchUiState.Idle)

    private var nextOffset = 0
    private var pagingJob: Job? = null

    init {
        loadFirstPage()
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun clearSearch() {
        Timber.d("item search cleared")
        _query.value = ""
    }

    fun retrySearch() {
        Timber.i("item search retry requested q=%s", _query.value)
        _searchRetry.value += 1
    }

    /** Selects (or clears, when null) the active category filter. */
    fun selectCategory(category: ItemCategory?) {
        val current = _state.value
        if (current is UiState.Success) {
            Timber.d("item category -> %s", category?.slug ?: "all")
            _state.value = UiState.Success(current.data.copy(category = category))
        }
    }

    private fun loadFirstPage() {
        nextOffset = 0
        _state.value = UiState.Loading
        pagingJob?.cancel()
        pagingJob = viewModelScope.launch {
            try {
                Timber.d("items loadFirstPage offset=%d size=%d", nextOffset, PAGE_SIZE)
                val page = getItemPage(nextOffset, PAGE_SIZE)
                ensureActive()
                nextOffset += page.size
                val end = page.size < PAGE_SIZE
                Timber.d("items loadFirstPage done got=%d nextOffset=%d endReached=%b", page.size, nextOffset, end)
                _state.value = UiState.Success(ItemsListData(items = page, endReached = end))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "items loadFirstPage failed offset=%d", nextOffset)
                _state.value = UiState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    fun loadMore() {
        val current = _state.value
        if (current !is UiState.Success) return
        val data = current.data
        if (data.isAppending || data.endReached) {
            Timber.d("items loadMore skipped: isAppending=%b endReached=%b", data.isAppending, data.endReached)
            return
        }
        pagingJob?.cancel()
        pagingJob = viewModelScope.launch {
            _state.value = UiState.Success(data.copy(isAppending = true, appendError = false))
            try {
                Timber.d("items loadMore offset=%d size=%d (have=%d)", nextOffset, PAGE_SIZE, data.items.size)
                val page = getItemPage(nextOffset, PAGE_SIZE)
                ensureActive()
                nextOffset += page.size
                val end = page.size < PAGE_SIZE
                Timber.d("items loadMore done got=%d total=%d nextOffset=%d endReached=%b", page.size, data.items.size + page.size, nextOffset, end)
                _state.value = UiState.Success(data.copy(items = data.items + page, isAppending = false, endReached = end))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "items loadMore failed offset=%d", nextOffset)
                _state.value = UiState.Success(data.copy(isAppending = false, appendError = true))
            }
        }
    }

    /** Retry the first page (error state) or the failed append (success tail). */
    fun retry() {
        if (_state.value is UiState.Error) loadFirstPage() else loadMore()
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
