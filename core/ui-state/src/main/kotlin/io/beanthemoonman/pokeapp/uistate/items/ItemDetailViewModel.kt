package io.beanthemoonman.pokeapp.uistate.items

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.beanthemoonman.pokeapp.domain.model.Item
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.usecase.GetItemDetailUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
  private val getItemDetail: GetItemDetailUseCase,
  savedStateHandle: SavedStateHandle,
) : ViewModel() {

  private val itemId: Int = savedStateHandle.get<Int>(ARG_ID) ?: 0

  private val _state = MutableStateFlow<UiState<Item>>(UiState.Loading)
  val state: StateFlow<UiState<Item>> = _state.asStateFlow()

  init {
    load()
  }

  fun retry() {
    Timber.i("item detail retry id=%d", itemId)
    load()
  }

  private fun load() {
    _state.value = UiState.Loading
    viewModelScope.launch {
      try {
        Timber.d("item detail loading id=%d", itemId)
        _state.value = UiState.Success(getItemDetail(itemId))
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Timber.e(e, "item detail load failed id=%d", itemId)
        _state.value = UiState.Error(e.message ?: "Something went wrong")
      }
    }
  }

  companion object {
    /** Nav argument key for the item id; shared by both app targets. */
    const val ARG_ID = "id"
  }
}
