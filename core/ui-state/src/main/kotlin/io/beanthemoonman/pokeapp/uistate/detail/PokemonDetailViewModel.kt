package io.beanthemoonman.pokeapp.uistate.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.beanthemoonman.pokeapp.domain.model.PokemonDetail
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.usecase.GetPokemonDetailFullUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val getPokemonDetailFull: GetPokemonDetailFullUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val pokemonId: Int = savedStateHandle.get<Int>(ARG_ID) ?: 0

    private val _state = MutableStateFlow<UiState<PokemonDetail>>(UiState.Loading)
    val state: StateFlow<UiState<PokemonDetail>> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() {
        Timber.i("detail retry id=%d", pokemonId)
        load()
    }

    private fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                Timber.d("detail loading id=%d", pokemonId)
                val detail = getPokemonDetailFull(pokemonId)
                Timber.d(
                    "detail loaded id=%d moves=%d evo=%d",
                    pokemonId, detail.moves.size, detail.evolution.size
                )
                _state.value = UiState.Success(detail)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "detail load failed id=%d", pokemonId)
                _state.value = UiState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    companion object {
        /** Nav argument key for the national dex id; shared by both app targets. */
        const val ARG_ID = "id"
    }
}
