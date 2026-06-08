package io.beanthemoonman.pokeapp.domain.usecase

import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.repository.PokemonRepository
import javax.inject.Inject

/** Fetches a single Pokémon's full detail (cache-first). */
class GetPokemonDetailUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(id: Int): Pokemon = repository.getPokemonDetail(id)
}
