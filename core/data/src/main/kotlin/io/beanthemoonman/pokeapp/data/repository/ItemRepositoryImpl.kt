package io.beanthemoonman.pokeapp.data.repository

import io.beanthemoonman.pokeapp.data.local.dao.ItemDao
import io.beanthemoonman.pokeapp.data.mapper.toDomain
import io.beanthemoonman.pokeapp.data.mapper.toEntity
import io.beanthemoonman.pokeapp.data.remote.PokeApiService
import io.beanthemoonman.pokeapp.data.remote.dto.NamedApiResourceDto
import io.beanthemoonman.pokeapp.domain.model.Item
import io.beanthemoonman.pokeapp.domain.repository.ItemRepository
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
 * Room is the single source of truth for items. The `/item` list endpoint supplies the
 * page's id set (in id order); per-item detail is cache-first, fetched only on a miss.
 */
@Singleton
class ItemRepositoryImpl @Inject constructor(
    private val api: PokeApiService,
    private val itemDao: ItemDao
) : ItemRepository {

    private val nameIndexMutex = Mutex()
    @Volatile private var nameIndex: List<NamedApiResourceDto>? = null

    override suspend fun getItemPage(offset: Int, count: Int): List<Item> =
        withContext(Dispatchers.IO) {
            if (count <= 0) {
                Timber.w("getItemPage no-op: count=%d (offset=%d)", count, offset)
                return@withContext emptyList()
            }
            // The list endpoint gives this page's refs (id-ordered); detail is cache-first.
            val refs = api.getItemList(limit = count, offset = offset).results
            val ids = refs.map { it.id }
            if (ids.isEmpty()) {
                Timber.d("getItemPage offset=%d returned no refs (end of dictionary)", offset)
                return@withContext emptyList()
            }

            val cachedIds = itemDao.getByIds(ids).map { it.id }.toSet()
            val missing = ids.filterNot { it in cachedIds }
            Timber.d("getItemPage offset=%d count=%d cached=%d missing=%d", offset, count, cachedIds.size, missing.size)
            if (missing.isNotEmpty()) {
                val fetched = missing.chunked(DETAIL_FETCH_CHUNK).flatMap { chunk ->
                    coroutineScope {
                        chunk.map { id -> async { api.getItem(id) } }.awaitAll()
                    }
                }
                itemDao.upsertAll(fetched.map { it.toEntity(System.currentTimeMillis()) })
                Timber.d("getItemPage cached %d newly fetched items", fetched.size)
            }

            // Preserve the page's id order (matches the id-ascending list + getByIds order).
            itemDao.getByIds(ids).map { it.toDomain() }
                .also { Timber.d("getItemPage returning %d items (offset=%d)", it.size, offset) }
        }

    override suspend fun searchItems(query: String): List<Item> =
        withContext(Dispatchers.IO) {
            val q = query.trim()
            if (q.isEmpty()) return@withContext emptyList()

            // Numeric query → exact id lookup (cache-first, single entry).
            q.toIntOrNull()?.let { id ->
                return@withContext runCatching { getItemDetail(id) }
                    .onFailure { Timber.w(it, "searchItems id=%d lookup failed", id) }
                    .getOrNull()
                    ?.let { listOf(it) }
                    .orEmpty()
            }

            // Name query → substring match against the cached item name index.
            val matches = loadNameIndex()
                .asSequence()
                .filter { it.name.contains(q, ignoreCase = true) }
                .sortedWith(
                    compareByDescending<NamedApiResourceDto> { it.name.startsWith(q, ignoreCase = true) }
                        .thenBy { it.id }
                )
                .take(SEARCH_RESULT_LIMIT)
                .toList()
            Timber.d("searchItems name q=%s matches=%d", q, matches.size)
            if (matches.isEmpty()) return@withContext emptyList()

            coroutineScope {
                matches
                    .map { ref ->
                        async {
                            runCatching { getItemDetail(ref.id) }
                                .onFailure { Timber.w(it, "searchItems detail id=%d failed", ref.id) }
                                .getOrNull()
                        }
                    }
                    .awaitAll()
                    .filterNotNull()
            }
        }

    /**
     * The full item name index (`{ name, id }` for every item), fetched once and held in
     * memory. Guarded by a mutex so concurrent searches don't trigger duplicate fetches.
     */
    private suspend fun loadNameIndex(): List<NamedApiResourceDto> {
        nameIndex?.let { return it }
        return nameIndexMutex.withLock {
            nameIndex ?: run {
                Timber.d("loadNameIndex fetching item index (limit=%d)", NAME_INDEX_LIMIT)
                api.getItemList(limit = NAME_INDEX_LIMIT, offset = 0).results.also { nameIndex = it }
            }
        }
    }

    override suspend fun getItemDetail(id: Int): Item = withContext(Dispatchers.IO) {
        itemDao.getById(id)?.let { return@withContext it.toDomain() }
        val entity = api.getItem(id).toEntity(System.currentTimeMillis())
        itemDao.upsert(entity)
        entity.toDomain()
    }

    private companion object {
        const val DETAIL_FETCH_CHUNK = 12
        const val SEARCH_RESULT_LIMIT = 20
        /** Generous cap; the item dictionary is ~2050 entries. */
        const val NAME_INDEX_LIMIT = 10_000
    }
}
