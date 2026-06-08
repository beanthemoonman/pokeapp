package io.beanthemoonman.pokeapp.phone.ui.moves

import io.beanthemoonman.pokeapp.domain.model.Move
import io.beanthemoonman.pokeapp.domain.model.MoveCategory

/**
 * Paged moves-list state. [moves] is everything loaded so far; [category] (null = All)
 * filters the displayed rows client-side via [visible] by damage class. Paging continues
 * over the full generation-scoped dictionary regardless of the active filter.
 */
data class MovesListData(
    val moves: List<Move>,
    val category: MoveCategory? = null,
    val isAppending: Boolean = false,
    val appendError: Boolean = false,
    val endReached: Boolean = false,
) {
    val visible: List<Move>
        get() = if (category == null) moves else moves.filter { it.category == category }
}
