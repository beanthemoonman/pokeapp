package io.beanthemoonman.pokeapp.data.repository

import io.beanthemoonman.pokeapp.data.local.dao.PokemonDao
import io.beanthemoonman.pokeapp.data.local.dao.TeamDao
import io.beanthemoonman.pokeapp.data.local.entity.TeamEntity
import io.beanthemoonman.pokeapp.data.mapper.toDomain
import io.beanthemoonman.pokeapp.data.mapper.toEntity
import io.beanthemoonman.pokeapp.data.remote.PokeApiService
import io.beanthemoonman.pokeapp.data.remote.dto.NamedApiResourceDto
import io.beanthemoonman.pokeapp.domain.model.Generations
import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.repository.PokemonRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    private val nameIndexMutex = Mutex()
    @Volatile private var nameIndex: List<NamedApiResourceDto>? = null

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

    override suspend fun searchPokemon(query: String, dexEnd: Int): List<Pokemon> =
        withContext(Dispatchers.IO) {
            val q = query.trim()
            if (q.isEmpty()) return@withContext emptyList()

            // Numeric query → exact dex-number lookup (cache-first, single entry).
            q.toIntOrNull()?.let { number ->
                if (number !in 1..dexEnd) {
                    Timber.d("searchPokemon number=%d out of range (dexEnd=%d)", number, dexEnd)
                    return@withContext emptyList()
                }
                return@withContext runCatching { getPokemonDetail(number) }
                    .onFailure { Timber.w(it, "searchPokemon number=%d lookup failed", number) }
                    .getOrNull()
                    ?.let { listOf(it) }
                    .orEmpty()
            }

            // Name query → substring match against the cached National Dex name index.
            // Prefix matches rank above looser substring matches; ties break by dex id.
            val matches = loadNameIndex()
                .asSequence()
                .filter { it.id in 1..dexEnd && it.name.contains(q, ignoreCase = true) }
                .sortedWith(
                    compareByDescending<NamedApiResourceDto> { it.name.startsWith(q, ignoreCase = true) }
                        .thenBy { it.id }
                )
                .take(SEARCH_RESULT_LIMIT)
                .toList()
            Timber.d("searchPokemon name q=%s dexEnd=%d matches=%d", q, dexEnd, matches.size)
            if (matches.isEmpty()) return@withContext emptyList()

            // Fetch full detail for each match (cache-first), preserving the ranked order.
            coroutineScope {
                matches
                    .map { ref ->
                        async {
                            runCatching { getPokemonDetail(ref.id) }
                                .onFailure { Timber.w(it, "searchPokemon detail id=%d failed", ref.id) }
                                .getOrNull()
                        }
                    }
                    .awaitAll()
                    .filterNotNull()
            }
        }

    /**
     * The National Dex name index (`{ name, id }` for every entry), fetched once and held
     * in memory. The `/pokemon` list endpoint returns entries in dex order, so the first
     * [Generations.latest] dexEnd results cover every selectable generation. Guarded by a
     * mutex so concurrent searches don't trigger duplicate fetches.
     */
    private suspend fun loadNameIndex(): List<NamedApiResourceDto> {
        nameIndex?.let { return it }
        return nameIndexMutex.withLock {
            nameIndex ?: run {
                val limit = Generations.latest.dexEnd
                Timber.d("loadNameIndex fetching %d entries", limit)
                api.getPokemonList(limit = limit, offset = 0).results.also { nameIndex = it }
            }
        }
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
        const val SEARCH_RESULT_LIMIT = 20
    }
}
