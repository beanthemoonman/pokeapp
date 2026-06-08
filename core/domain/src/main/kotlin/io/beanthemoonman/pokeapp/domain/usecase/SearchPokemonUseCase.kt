package io.beanthemoonman.pokeapp.domain.usecase

import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.repository.PokemonRepository
import javax.inject.Inject

/**
 * Searches the active generation's dex (`1..dexEnd`) by name or dex number.
 * Trims the query and short-circuits a blank one to an empty result.
 */
class SearchPokemonUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(query: String, dexEnd: Int): List<Pokemon> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        return repository.searchPokemon(trimmed, dexEnd)
    }
}
