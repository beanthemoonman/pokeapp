package io.beanthemoonman.pokeapp.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Persists the user's chosen generation (the global app context).
 * A null id means no generation has been selected yet (first launch).
 */
interface GenerationRepository {
  val selectedGenerationId: Flow<Int?>
  suspend fun selectGeneration(id: Int)
}
