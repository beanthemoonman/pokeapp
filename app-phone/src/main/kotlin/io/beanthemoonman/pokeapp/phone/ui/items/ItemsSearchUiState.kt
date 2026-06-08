package io.beanthemoonman.pokeapp.phone.ui.items

import io.beanthemoonman.pokeapp.domain.model.Item

/** Search box state for the items list, independent of the paged dictionary. */
sealed interface ItemsSearchUiState {
    /** Blank query — the paged dictionary is shown instead of results. */
    data object Idle : ItemsSearchUiState
    data object Loading : ItemsSearchUiState
    data object Empty : ItemsSearchUiState
    data class Error(val message: String) : ItemsSearchUiState
    data class Results(val items: List<Item>) : ItemsSearchUiState
}
