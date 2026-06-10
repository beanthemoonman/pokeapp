package io.beanthemoonman.pokeapp.domain.repository

import io.beanthemoonman.pokeapp.domain.model.Item

/**
 * Single source of truth for dictionary items. Implemented in core/data.
 * Implementations check the local cache first and fetch from the API on miss.
 *
 * Items are NOT generation-scoped: PokéAPI has no reliable introduced-in-gen list,
 * so the dictionary is the full (national) item set. The selected generation only
 * drives the header chrome, not which items are returned.
 */
interface ItemRepository {
  /**
   * One page of the item dictionary: [count] items starting at list [offset]
   * (PokéAPI `/item?limit&offset` order). Cache-first per entry — cached rows are
   * returned directly, misses are fetched and cached. A short page signals the end.
   */
  suspend fun getItemPage(offset: Int, count: Int): List<Item>

  /**
   * Searches the dictionary by name (case-insensitive substring) or by exact id.
   * Returns full [Item]s — name-prefix matches first, then by id — capped to a small
   * result set. A blank query yields an empty list.
   */
  suspend fun searchItems(query: String): List<Item>

  /** Full detail for one item, cache-first. */
  suspend fun getItemDetail(id: Int): Item
}
