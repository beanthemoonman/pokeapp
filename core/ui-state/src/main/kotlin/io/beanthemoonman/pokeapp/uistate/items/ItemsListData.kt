package io.beanthemoonman.pokeapp.uistate.items

import io.beanthemoonman.pokeapp.domain.model.Item
import io.beanthemoonman.pokeapp.domain.model.ItemCategory

/**
 * Paged items-list state. [items] is everything loaded so far; [category] (null = All)
 * filters the displayed rows client-side via [visible]. Paging continues over the full
 * dictionary regardless of the active filter.
 */
data class ItemsListData(
    val items: List<Item>,
    val category: ItemCategory? = null,
    val isAppending: Boolean = false,
    val appendError: Boolean = false,
    val endReached: Boolean = false,
) {
    val visible: List<Item>
        get() = if (category == null) items else items.filter { it.category == category }
}
