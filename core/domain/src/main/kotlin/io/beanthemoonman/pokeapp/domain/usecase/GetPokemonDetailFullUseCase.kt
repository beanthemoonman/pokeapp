package io.beanthemoonman.pokeapp.domain.usecase

import io.beanthemoonman.pokeapp.domain.model.PokemonDetail
import io.beanthemoonman.pokeapp.domain.repository.PokemonRepository
import javax.inject.Inject

/** Fetches a single Pokémon's full detail aggregate (cache-first). */
class GetPokemonDetailFullUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(id: Int): PokemonDetail = repository.getPokemonDetailFull(id)
}
