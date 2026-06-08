package io.beanthemoonman.pokeapp.domain.repository

import io.beanthemoonman.pokeapp.domain.model.Pokemon
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for Pokémon data. Implemented in core/data.
 * Implementations check the local cache first and fetch from the API on miss.
 */
interface PokemonRepository {
    /**
     * One page of the National Dex: entries with id in `[startId, startId + count)`.
     * Cache-first per entry — cached rows are returned directly, misses are fetched and cached.
     */
    suspend fun getPokemonPage(startId: Int, count: Int): List<Pokemon>
    suspend fun getPokemonDetail(id: Int): Pokemon
    fun getTeam(): Flow<List<Pokemon?>>
    suspend fun setTeamSlot(slot: Int, pokemonId: Int?)
}
