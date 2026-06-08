package io.beanthemoonman.pokeapp.domain.usecase

import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.repository.PokemonRepository
import javax.inject.Inject

/**
 * Loads one page of the National Dex, clamped to the active generation's [dexEnd].
 * Returns an empty list once paging has run past the end of the dex.
 */
class GetPokemonPageUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(startId: Int, count: Int, dexEnd: Int): List<Pokemon> {
        if (startId > dexEnd) return emptyList()
        val clampedCount = minOf(count, dexEnd - startId + 1)
        return repository.getPokemonPage(startId, clampedCount)
    }

    companion object {
        const val PAGE_SIZE = 30
    }
}
