package io.beanthemoonman.pokeapp.domain.repository

import io.beanthemoonman.pokeapp.domain.model.Move

/**
 * Single source of truth for dictionary moves. Implemented in core/data.
 * Implementations check the local cache first and fetch from the API on miss.
 *
 * Moves ARE generation-scoped (unlike items): a move belongs to the dictionary for
 * generation X only when its introduction generation is `<= X`. The scoped id set is
 * derived from PokéAPI's `/generation/{id}` move lists (union of generations `1..X`).
 */
interface MoveRepository {
  /**
   * One page of the move dictionary scoped to [generationId]: [count] moves starting
   * at [offset] within the generation-scoped, id-ordered move set. Cache-first per
   * entry — cached rows are returned directly, misses are fetched and cached. A short
   * page signals the end of the scoped dictionary.
   */
  suspend fun getMovePage(generationId: Int, offset: Int, count: Int): List<Move>

  /**
   * Searches the [generationId]-scoped dictionary by name (case-insensitive substring)
   * or by exact id. Returns full [Move]s — name-prefix matches first, then by id —
   * capped to a small result set. A blank query yields an empty list.
   */
  suspend fun searchMoves(generationId: Int, query: String): List<Move>

  /** Full detail for one move, cache-first. */
  suspend fun getMoveDetail(id: Int): Move
}
