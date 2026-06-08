package io.beanthemoonman.pokeapp.phone.ui.list

import io.beanthemoonman.pokeapp.domain.model.Pokemon

/**
 * Payload for the list's [io.beanthemoonman.pokeapp.domain.model.UiState.Success] state.
 * The first page maps to Loading/Error/Success; subsequent pages append into [items]
 * with [isAppending] / [appendError] / [endReached] describing the paging tail.
 */
data class PokemonListData(
    val items: List<Pokemon>,
    val isAppending: Boolean = false,
    val appendError: Boolean = false,
    val endReached: Boolean = false,
)
