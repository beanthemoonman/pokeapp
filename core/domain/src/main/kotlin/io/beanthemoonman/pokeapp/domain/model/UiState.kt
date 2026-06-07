package io.beanthemoonman.pokeapp.domain.model

/**
 * Canonical UI state wrapper consumed by every screen ViewModel.
 * All screens must handle Loading, Success, and Error.
 */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
