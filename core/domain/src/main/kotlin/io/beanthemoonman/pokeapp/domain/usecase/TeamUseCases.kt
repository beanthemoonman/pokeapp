package io.beanthemoonman.pokeapp.domain.usecase

import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes the saved team as six ordered slots (index 0–5); a null entry is an empty slot.
 * The list is always [TEAM_SIZE] long.
 */
class ObserveTeamUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    operator fun invoke(): Flow<List<Pokemon?>> = repository.getTeam()

    companion object {
        const val TEAM_SIZE = 6
    }
}

/** Assigns (or clears, with a null [pokemonId]) the Pokémon in a team [slot] (0–5). */
class SetTeamSlotUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(slot: Int, pokemonId: Int?) = repository.setTeamSlot(slot, pokemonId)
}
