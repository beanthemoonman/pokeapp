package io.beanthemoonman.pokeapp.data.repository

import io.beanthemoonman.pokeapp.data.local.dao.PokemonDao
import io.beanthemoonman.pokeapp.data.local.dao.TeamDao
import io.beanthemoonman.pokeapp.data.local.entity.TeamEntity
import io.beanthemoonman.pokeapp.data.mapper.toDomain
import io.beanthemoonman.pokeapp.data.mapper.toEntity
import io.beanthemoonman.pokeapp.data.remote.PokeApiService
import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.repository.PokemonRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room is the single source of truth. The list/detail reads always come from Room;
 * the network is used only to refresh the cache on a miss or stale entry.
 */
@Singleton
class PokemonRepositoryImpl @Inject constructor(
    private val api: PokeApiService,
    private val pokemonDao: PokemonDao,
    private val teamDao: TeamDao
) : PokemonRepository {

    override fun getPokemonList(limit: Int, offset: Int): Flow<List<Pokemon>> = flow {
        // Best-effort refresh of this page; on failure we fall back to whatever is cached.
        try {
            val page = api.getPokemonList(limit, offset)
            // Fetch detail in bounded chunks so we never fan out hundreds of parallel calls.
            val details = page.results.chunked(DETAIL_FETCH_CHUNK).flatMap { chunk ->
                coroutineScope {
                    chunk.map { ref -> async { api.getPokemonDetail(ref.id) } }.awaitAll()
                }
            }
            pokemonDao.upsertAll(details.map { it.toEntity(System.currentTimeMillis()) })
        } catch (_: Exception) {
            // Offline / API error: serve the cached page below.
        }
        emitAll(
            pokemonDao.pageFlow(limit, offset).map { rows -> rows.map { it.toDomain() } }
        )
    }.flowOn(Dispatchers.IO)

    override suspend fun getPokemonDetail(id: Int): Pokemon {
        pokemonDao.getById(id)?.let { return it.toDomain() }
        val entity = api.getPokemonDetail(id).toEntity(System.currentTimeMillis())
        pokemonDao.upsert(entity)
        return entity.toDomain()
    }

    override fun getTeam(): Flow<List<Pokemon?>> =
        teamDao.getTeamFlow().map { rows ->
            (0 until TEAM_SIZE).map { slot ->
                val pokemonId = rows.firstOrNull { it.teamSlot == slot }?.pokemonId
                pokemonId?.let { pokemonDao.getById(it)?.toDomain() }
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun setTeamSlot(slot: Int, pokemonId: Int?) {
        require(slot in 0 until TEAM_SIZE) { "Team slot must be in 0..${TEAM_SIZE - 1}" }
        teamDao.setSlot(TeamEntity(teamSlot = slot, pokemonId = pokemonId))
    }

    private companion object {
        const val TEAM_SIZE = 6
        const val DETAIL_FETCH_CHUNK = 12
    }
}
