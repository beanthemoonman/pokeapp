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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
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

    override suspend fun getPokemonPage(startId: Int, count: Int): List<Pokemon> =
        withContext(Dispatchers.IO) {
            if (count <= 0) {
                Timber.w("getPokemonPage no-op: count=%d (start=%d)", count, startId)
                return@withContext emptyList()
            }
            val ids = (startId until startId + count).toList()

            // Cache-first: only the ids missing from Room are fetched (in bounded chunks),
            // then upserted. A failure here propagates so the caller can show a page error.
            val cachedIds = pokemonDao.getByIds(ids).map { it.id }.toSet()
            val missing = ids.filterNot { it in cachedIds }
            Timber.d("getPokemonPage start=%d count=%d cached=%d missing=%d", startId, count, cachedIds.size, missing.size)
            if (missing.isNotEmpty()) {
                Timber.d("getPokemonPage fetching %d ids from API (chunks of %d)", missing.size, DETAIL_FETCH_CHUNK)
                val fetched = missing.chunked(DETAIL_FETCH_CHUNK).flatMap { chunk ->
                    coroutineScope {
                        chunk.map { id -> async { api.getPokemonDetail(id) } }.awaitAll()
                    }
                }
                pokemonDao.upsertAll(fetched.map { it.toEntity(System.currentTimeMillis()) })
                Timber.d("getPokemonPage cached %d newly fetched entries", fetched.size)
            }

            pokemonDao.getByIds(ids).map { it.toDomain() }
                .also { Timber.d("getPokemonPage returning %d entries (start=%d)", it.size, startId) }
        }

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
