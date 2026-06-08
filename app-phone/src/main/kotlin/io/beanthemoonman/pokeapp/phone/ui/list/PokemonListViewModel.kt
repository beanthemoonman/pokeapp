package io.beanthemoonman.pokeapp.phone.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.beanthemoonman.pokeapp.domain.model.Generation
import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.usecase.GetPokemonListUseCase
import io.beanthemoonman.pokeapp.domain.usecase.ObserveSelectedGenerationUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val getPokemonList: GetPokemonListUseCase,
    observeSelectedGeneration: ObserveSelectedGenerationUseCase,
) : ViewModel() {

    /** Active generation context (drives the header + which dex is loaded). */
    val generation: StateFlow<Generation?> = observeSelectedGeneration()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val retry = MutableStateFlow(0)

    private val _state = MutableStateFlow<UiState<List<Pokemon>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Pokemon>>> = _state.asStateFlow()

    init {
        combine(generation.filterNotNull(), retry) { gen, _ -> gen }
            .flatMapLatest { gen ->
                // National Dex #1..dexEnd for the selected generation.
                getPokemonList(gen.dexEnd, 0)
                    .map { pokemon ->
                        if (pokemon.isEmpty()) UiState.Error("No Pokémon available")
                        else UiState.Success(pokemon)
                    }
                    .onStart { emit(UiState.Loading) }
                    .catch { e -> emit(UiState.Error(e.message ?: "Something went wrong")) }
            }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun load() {
        retry.value += 1
    }
}
