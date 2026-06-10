package io.beanthemoonman.pokeapp.data.repository

import io.beanthemoonman.pokeapp.data.local.dao.MoveDao
import io.beanthemoonman.pokeapp.data.mapper.toDomain
import io.beanthemoonman.pokeapp.data.mapper.toEntity
import io.beanthemoonman.pokeapp.data.remote.PokeApiService
import io.beanthemoonman.pokeapp.data.remote.dto.NamedApiResourceDto
import io.beanthemoonman.pokeapp.domain.model.Move
import io.beanthemoonman.pokeapp.domain.repository.MoveRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room is the single source of truth for moves. The generation-scoped id set comes from
 * PokéAPI's `/generation/{id}` move lists (union of generations `1..genId`); per-move
 * detail is cache-first, fetched only on a miss.
 */
@Singleton
class MoveRepositoryImpl @Inject constructor(
  private val api: PokeApiService,
  private val moveDao: MoveDao
) : MoveRepository {

  private val genMutex = Mutex()

  /** Per-generation move refs (moves *introduced* in that gen), memoized across calls. */
  private val genMoves = HashMap<Int, List<NamedApiResourceDto>>()

  override suspend fun getMovePage(generationId: Int, offset: Int, count: Int): List<Move> =
    withContext(Dispatchers.IO) {
      if (count <= 0) {
        Timber.w("getMovePage no-op: count=%d (gen=%d offset=%d)", count, generationId, offset)
        return@withContext emptyList()
      }
      val scoped = scopedRefs(generationId)
      val window = scoped.drop(offset).take(count)
      val ids = window.map { it.id }
      if (ids.isEmpty()) {
        Timber.d(
          "getMovePage gen=%d offset=%d past end (scoped=%d)",
          generationId,
          offset,
          scoped.size
        )
        return@withContext emptyList()
      }

      val cachedIds = moveDao.getByIds(ids).map { it.id }.toSet()
      val missing = ids.filterNot { it in cachedIds }
      Timber.d(
        "getMovePage gen=%d offset=%d count=%d cached=%d missing=%d",
        generationId,
        offset,
        count,
        cachedIds.size,
        missing.size
      )
      if (missing.isNotEmpty()) {
        val fetched = missing.chunked(DETAIL_FETCH_CHUNK).flatMap { chunk ->
          coroutineScope {
            chunk.map { id -> async { api.getMoveDetail(id) } }.awaitAll()
          }
        }
        moveDao.upsertAll(fetched.map { it.toEntity(System.currentTimeMillis()) })
        Timber.d("getMovePage cached %d newly fetched moves", fetched.size)
      }

      // Preserve the window's id order (matches the id-ascending scoped set + getByIds order).
      moveDao.getByIds(ids).map { it.toDomain() }
        .also {
          Timber.d(
            "getMovePage returning %d moves (gen=%d offset=%d)",
            it.size,
            generationId,
            offset
          )
        }
    }

  override suspend fun searchMoves(generationId: Int, query: String): List<Move> =
    withContext(Dispatchers.IO) {
      val q = query.trim()
      if (q.isEmpty()) return@withContext emptyList()
      val scoped = scopedRefs(generationId)

      // Numeric query → exact id lookup, but only if in this generation's scope.
      q.toIntOrNull()?.let { id ->
        if (scoped.none { it.id == id }) return@withContext emptyList()
        return@withContext runCatching { getMoveDetail(id) }
          .onFailure { Timber.w(it, "searchMoves id=%d lookup failed", id) }
          .getOrNull()
          ?.let { listOf(it) }
          .orEmpty()
      }

      // Name query → substring match against the gen-scoped move refs.
      val matches = scoped.asSequence()
        .filter { it.name.contains(q, ignoreCase = true) }
        .sortedWith(
          compareByDescending<NamedApiResourceDto> { it.name.startsWith(q, ignoreCase = true) }
            .thenBy { it.id }
        )
        .take(SEARCH_RESULT_LIMIT)
        .toList()
      Timber.d("searchMoves gen=%d name q=%s matches=%d", generationId, q, matches.size)
      if (matches.isEmpty()) return@withContext emptyList()

      coroutineScope {
        matches
          .map { ref ->
            async {
              runCatching { getMoveDetail(ref.id) }
                .onFailure { Timber.w(it, "searchMoves detail id=%d failed", ref.id) }
                .getOrNull()
            }
          }
          .awaitAll()
          .filterNotNull()
      }
    }

  override suspend fun getMoveDetail(id: Int): Move = withContext(Dispatchers.IO) {
    moveDao.getById(id)?.let { return@withContext it.toDomain() }
    val entity = api.getMoveDetail(id).toEntity(System.currentTimeMillis())
    moveDao.upsert(entity)
    entity.toDomain()
  }

  /**
   * The id-ordered move refs in scope for [generationId] = the union of moves introduced
   * in generations `1..generationId`. Each generation's list is fetched once and memoized;
   * concurrent callers are serialized so a generation is never fetched twice.
   */
  private suspend fun scopedRefs(generationId: Int): List<NamedApiResourceDto> =
    genMutex.withLock {
      (1..generationId).flatMap { gen ->
        genMoves.getOrPut(gen) {
          Timber.d("scopedRefs fetching /generation/%d move list", gen)
          api.getGeneration(gen).moves
        }
      }.distinctBy { it.id }.sortedBy { it.id }
    }

  private companion object {
    const val DETAIL_FETCH_CHUNK = 12
    const val SEARCH_RESULT_LIMIT = 20
  }
}
