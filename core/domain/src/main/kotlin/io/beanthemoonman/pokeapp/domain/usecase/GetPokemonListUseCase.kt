package io.beanthemoonman.pokeapp.domain.usecase

import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams a page of the Pokédex from the repository (Room-backed, network-refreshed). */
class GetPokemonListUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    operator fun invoke(limit: Int, offset: Int): Flow<List<Pokemon>> =
        repository.getPokemonList(limit, offset)
}
