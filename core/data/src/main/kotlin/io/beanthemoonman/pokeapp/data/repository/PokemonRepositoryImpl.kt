package io.beanthemoonman.pokeapp.data.repository

import com.google.gson.Gson
import io.beanthemoonman.pokeapp.data.local.dao.PokemonDao
import io.beanthemoonman.pokeapp.data.local.dao.PokemonDetailDao
import io.beanthemoonman.pokeapp.data.local.dao.TeamDao
import io.beanthemoonman.pokeapp.data.local.entity.TeamEntity
import io.beanthemoonman.pokeapp.data.mapper.englishFlavor
import io.beanthemoonman.pokeapp.data.mapper.englishGenus
import io.beanthemoonman.pokeapp.data.mapper.englishShortEffect
import io.beanthemoonman.pokeapp.data.mapper.toDomain
import io.beanthemoonman.pokeapp.data.mapper.toEntity
import io.beanthemoonman.pokeapp.data.mapper.toMoveInfo
import io.beanthemoonman.pokeapp.data.mapper.toTitle
import io.beanthemoonman.pokeapp.data.remote.PokeApiService
import io.beanthemoonman.pokeapp.data.remote.dto.ChainLinkDto
import io.beanthemoonman.pokeapp.data.remote.dto.EvolutionDetailDto
import io.beanthemoonman.pokeapp.data.remote.dto.NamedApiResourceDto
import io.beanthemoonman.pokeapp.data.remote.dto.PokemonDetailDto
import io.beanthemoonman.pokeapp.data.remote.dto.PokemonSpeciesDto
import io.beanthemoonman.pokeapp.domain.model.AbilityInfo
import io.beanthemoonman.pokeapp.domain.model.EvolutionStage
import io.beanthemoonman.pokeapp.domain.model.Generations
import io.beanthemoonman.pokeapp.domain.model.MegaForm
import io.beanthemoonman.pokeapp.domain.model.MoveInfo
import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.model.PokemonDetail
import io.beanthemoonman.pokeapp.domain.repository.PokemonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
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
  private val detailDao: PokemonDetailDao,
  private val teamDao: TeamDao,
  private val gson: Gson
) : PokemonRepository {

  private val nameIndexMutex = Mutex()
  @Volatile
  private var nameIndex: List<NamedApiResourceDto>? = null

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
      Timber.d(
        "getPokemonPage start=%d count=%d cached=%d missing=%d",
        startId,
        count,
        cachedIds.size,
        missing.size
      )
      if (missing.isNotEmpty()) {
        Timber.d(
          "getPokemonPage fetching %d ids from API (chunks of %d)",
          missing.size,
          DETAIL_FETCH_CHUNK
        )
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

  override suspend fun getPokemonDetailFull(id: Int): PokemonDetail =
    withContext(Dispatchers.IO) {
      // Cache hit: rebuild the aggregate from the cached detail row + cached base.
      detailDao.getById(id)?.let { cached ->
        Timber.d("getPokemonDetailFull cache hit id=%d", id)
        return@withContext cached.toDomain(getPokemonDetail(id), gson)
      }

      // Miss: one /pokemon fetch supplies both the base row and the moves/species refs.
      Timber.d("getPokemonDetailFull cache miss id=%d; fetching detail+species+evo+moves", id)
      val now = System.currentTimeMillis()
      val dto = api.getPokemonDetail(id)
      val baseEntity = dto.toEntity(now)
      pokemonDao.upsert(baseEntity)

      val speciesId = dto.species?.id ?: id
      val species = api.getPokemonSpecies(speciesId)
      val abilities = resolveAbilities(dto)
      val moves = resolveLevelUpMoves(dto)
      val evolution = species.evolutionChain?.id
        ?.let { chainId ->
          runCatching { buildEvolution(chainId) }
            .onFailure { Timber.w(it, "evolution chain %d failed", chainId) }
            .getOrDefault(emptyList())
        }
        .orEmpty()
      val megaForms = runCatching { buildMegaForms(species) }
        .onFailure { Timber.w(it, "mega forms for species %d failed", speciesId) }
        .getOrDefault(emptyList())
      Timber.d(
        "getPokemonDetailFull id=%d assembled: moves=%d evoStages=%d abilities=%d megas=%d",
        id, moves.size, evolution.size, dto.abilities.size, megaForms.size
      )

      val detail = PokemonDetail(
        pokemon = baseEntity.toDomain(),
        genus = species.englishGenus(),
        flavorText = species.englishFlavor(),
        abilities = abilities,
        captureRate = species.captureRate,
        moves = moves,
        evolution = evolution,
        megaForms = megaForms
      )
      detailDao.upsert(detail.toEntity(gson, now))
      detail
    }

  /**
   * The Pokémon's abilities (in slot order), each enriched with its English short-effect
   * text from `/ability/{name}`, fetched concurrently. A failed effect fetch keeps the
   * ability with blank effect text rather than dropping it.
   */
  private suspend fun resolveAbilities(dto: PokemonDetailDto): List<AbilityInfo> =
    coroutineScope {
      dto.abilities.map { slot ->
        async {
          val shortEffect = runCatching { api.getAbility(slot.ability.name).englishShortEffect() }
            .onFailure { Timber.w(it, "ability %s effect fetch failed", slot.ability.name) }
            .getOrDefault("")
          AbilityInfo(
            name = slot.ability.name.toTitle(),
            shortEffect = shortEffect,
            isHidden = slot.isHidden,
          )
        }
      }.awaitAll()
    }

  /**
   * The Pokémon's level-up moves for the most recent version group it appears in,
   * sorted by the level learned (then name) and capped. Each move's combat stats are
   * fetched from `/move/{name}` concurrently; failures drop that single move.
   */
  private suspend fun resolveLevelUpMoves(dto: PokemonDetailDto): List<MoveInfo> {
    val byMinLevel = dto.moves.mapNotNull { slot ->
      val level = slot.versionGroupDetails
        .filter { it.moveLearnMethod.name == LEVEL_UP }
        .minOfOrNull { it.levelLearnedAt }
        ?: return@mapNotNull null
      slot.move.name to level
    }
    val capped = byMinLevel
      .sortedWith(compareBy({ it.second }, { it.first }))
      .take(MOVES_LIMIT)
    if (capped.isEmpty()) return emptyList()

    return coroutineScope {
      capped
        .map { (name, level) ->
          async {
            runCatching { api.getMove(name).toMoveInfo(level) }
              .onFailure { Timber.w(it, "move %s fetch failed", name) }
              .getOrNull()
          }
        }
        .awaitAll()
        .filterNotNull()
    }
  }

  /**
   * Builds the evolution chain as a [EvolutionStage] tree (one root per chain), preserving
   * branches so lines like Poliwhirl → Poliwrath / Politoed render their split. The chain is
   * pre-order capped at [EVO_LIMIT] distinct species; each stage's sprite/types come from the
   * cache-first base lookup (fetched concurrently up front), and the condition is a short label
   * for how the prior stage evolves into it.
   */
  private suspend fun buildEvolution(chainId: Int): List<EvolutionStage> {
    val chain = api.getEvolutionChain(chainId)

    // Walk pre-order to collect a bounded, distinct set of species ids, then fetch their
    // bases concurrently before assembling the tree from the cached results.
    val ids = LinkedHashSet<Int>()
    fun collect(link: ChainLinkDto) {
      ids += link.species.id
      link.evolvesTo.forEach(::collect)
    }
    collect(chain.chain)
    val allowed = ids.take(EVO_LIMIT).toSet()

    val bases = coroutineScope {
      allowed.map { stageId ->
        async {
          stageId to runCatching { getPokemonDetail(stageId) }
            .onFailure { Timber.w(it, "evo stage %d base fetch failed", stageId) }
            .getOrNull()
        }
      }.awaitAll().toMap()
    }

    fun build(link: ChainLinkDto, condition: String?): EvolutionStage? {
      val stageId = link.species.id
      if (stageId !in allowed) return null
      val base = bases[stageId]
      return EvolutionStage(
        id = stageId,
        name = base?.name ?: link.species.name.toTitle(),
        spriteUrl = base?.spriteUrl.orEmpty(),
        types = base?.types.orEmpty(),
        condition = condition,
        evolvesTo = link.evolvesTo.mapNotNull { next ->
          build(next, conditionLabel(next.evolutionDetails))
        }
      )
    }
    return listOfNotNull(build(chain.chain, null))
  }

  /**
   * The species' Mega Evolution / Primal Reversion forms (PokéAPI models these as non-default
   * `varieties`, not evolution-chain stages). Each form's sprite/types come from a cache-first
   * `/pokemon/{id}` fetch; a failed fetch drops that single form.
   */
  private suspend fun buildMegaForms(species: PokemonSpeciesDto): List<MegaForm> {
    val megaVarieties = species.varieties.filter { variety ->
      !variety.isDefault && variety.pokemon.name.let { "-mega" in it || "-primal" in it }
    }
    if (megaVarieties.isEmpty()) return emptyList()
    return coroutineScope {
      megaVarieties.map { variety ->
        async {
          val formId = variety.pokemon.id
          val base = runCatching { getPokemonDetail(formId) }
            .onFailure { Timber.w(it, "mega form %d fetch failed", formId) }
            .getOrNull() ?: return@async null
          MegaForm(
            id = formId,
            name = variety.pokemon.name.toTitle(),
            spriteUrl = base.spriteUrl,
            types = base.types,
          )
        }
      }.awaitAll().filterNotNull()
    }
  }

  /** Short human label for an evolution trigger; null when nothing useful is known. */
  private fun conditionLabel(details: List<EvolutionDetailDto>): String? {
    val d = details.firstOrNull() ?: return null
    return when {
      d.minLevel != null -> "Lv. ${d.minLevel}"
      d.item != null -> d.item.name.toTitle()
      d.heldItem != null -> "Hold ${d.heldItem.name.toTitle()}"
      d.minHappiness != null -> "Friendship"
      d.trigger?.name == "trade" -> "Trade"
      d.trigger != null -> d.trigger.name.toTitle()
      else -> null
    }
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
    const val LEVEL_UP = "level-up"

    /** Cap on level-up moves shown on the detail screen (keeps move fetches bounded). */
    const val MOVES_LIMIT = 24

    /** Cap on evolution stages (guards against very branched lines; Eevee needs base + 8). */
    const val EVO_LIMIT = 12
  }
}
