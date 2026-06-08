package io.beanthemoonman.pokeapp.phone.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.usecase.GetPokemonListUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val getPokemonList: GetPokemonListUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Pokemon>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Pokemon>>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        getPokemonList(GEN_I_COUNT, 0)
            .onEach { pokemon ->
                _state.value = if (pokemon.isEmpty()) {
                    UiState.Error("No Pokémon available")
                } else {
                    UiState.Success(pokemon)
                }
            }
            .catch { e -> _state.value = UiState.Error(e.message ?: "Something went wrong") }
            .launchIn(viewModelScope)
    }

    companion object {
        const val GEN_I_COUNT = 151
    }
}
