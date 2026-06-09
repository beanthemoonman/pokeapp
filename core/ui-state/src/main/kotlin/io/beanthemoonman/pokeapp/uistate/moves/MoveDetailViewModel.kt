package io.beanthemoonman.pokeapp.uistate.moves

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.beanthemoonman.pokeapp.domain.model.Move
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.usecase.GetMoveDetailUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MoveDetailViewModel @Inject constructor(
    private val getMoveDetail: GetMoveDetailUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val moveId: Int = savedStateHandle.get<Int>(ARG_ID) ?: 0

    private val _state = MutableStateFlow<UiState<Move>>(UiState.Loading)
    val state: StateFlow<UiState<Move>> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() {
        Timber.i("move detail retry id=%d", moveId)
        load()
    }

    private fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                Timber.d("move detail loading id=%d", moveId)
                _state.value = UiState.Success(getMoveDetail(moveId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "move detail load failed id=%d", moveId)
                _state.value = UiState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    companion object {
        /** Nav argument key for the move id; shared by both app targets. */
        const val ARG_ID = "id"
    }
}
