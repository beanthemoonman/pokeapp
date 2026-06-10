package io.beanthemoonman.pokeapp.data.mapper

import com.google.gson.Gson
import io.beanthemoonman.pokeapp.data.local.entity.PokemonDetailEntity
import io.beanthemoonman.pokeapp.data.local.entity.PokemonEntity
import io.beanthemoonman.pokeapp.data.remote.dto.AbilityDetailDto
import io.beanthemoonman.pokeapp.data.remote.dto.MoveDto
import io.beanthemoonman.pokeapp.data.remote.dto.PokemonDetailDto
import io.beanthemoonman.pokeapp.data.remote.dto.PokemonSpeciesDto
import io.beanthemoonman.pokeapp.domain.model.AbilityInfo
import io.beanthemoonman.pokeapp.domain.model.EvolutionStage
import io.beanthemoonman.pokeapp.domain.model.MegaForm
import io.beanthemoonman.pokeapp.domain.model.MoveCategory
import io.beanthemoonman.pokeapp.domain.model.MoveInfo
import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.model.PokemonDetail
import io.beanthemoonman.pokeapp.domain.model.Stats
import io.beanthemoonman.pokeapp.domain.model.Type

/** Ordered stat slugs as the PokéAPI returns them; index aligns with [PokemonEntity.stats]. */
private val STAT_ORDER = listOf(
  "hp", "attack", "defense", "special-attack", "special-defense", "speed"
)

fun PokemonDetailDto.toEntity(cachedAt: Long): PokemonEntity {
  val byStat = stats.associate { it.stat.name to it.baseStat }
  return PokemonEntity(
    id = id,
    name = name.replaceFirstChar { it.uppercase() },
    spriteUrl = sprites.other?.officialArtwork?.frontDefault
      ?: sprites.frontDefault.orEmpty(),
    types = types.sortedBy { it.slot }.mapNotNull { Type.fromApiName(it.type.name) },
    stats = STAT_ORDER.map { byStat[it] ?: 0 },
    height = height,
    weight = weight,
    cachedAt = cachedAt
  )
}

fun PokemonEntity.toDomain(): Pokemon = Pokemon(
  id = id,
  name = name,
  spriteUrl = spriteUrl,
  types = types,
  stats = Stats(
    hp = stats.getOrElse(0) { 0 },
    attack = stats.getOrElse(1) { 0 },
    defense = stats.getOrElse(2) { 0 },
    specialAttack = stats.getOrElse(3) { 0 },
    specialDefense = stats.getOrElse(4) { 0 },
    speed = stats.getOrElse(5) { 0 }
  ),
  height = height,
  weight = weight
)

// ── Full-detail mappers ──────────────────────────────────────────────────────

/** First English genus ("Flame Pokémon"), or a blank fallback. */
fun PokemonSpeciesDto.englishGenus(): String =
  genera.firstOrNull { it.language.name == ENGLISH }?.genus.orEmpty()

/** First English flavor text, with the API's hard line breaks normalized to spaces. */
fun PokemonSpeciesDto.englishFlavor(): String =
  flavorTextEntries.firstOrNull { it.language.name == ENGLISH }
    ?.flavorText
    ?.replace('\n', ' ')
    ?.replace('', ' ') // form feed PokéAPI uses between words
    ?.trim()
    .orEmpty()

fun MoveDto.toMoveInfo(level: Int): MoveInfo = MoveInfo(
  name = name.toTitle(),
  type = Type.fromApiName(type.name),
  category = when (damageClass?.name) {
    "physical" -> MoveCategory.PHYSICAL
    "special" -> MoveCategory.SPECIAL
    else -> MoveCategory.STATUS
  },
  power = power,
  accuracy = accuracy,
  pp = pp ?: 0,
  level = level
)

fun PokemonDetail.toEntity(gson: Gson, cachedAt: Long): PokemonDetailEntity = PokemonDetailEntity(
  id = pokemon.id,
  genus = genus,
  flavorText = flavorText,
  abilities = gson.toJson(abilities),
  captureRate = captureRate,
  movesJson = gson.toJson(moves),
  evolutionJson = gson.toJson(evolution),
  megaJson = gson.toJson(megaForms),
  cachedAt = cachedAt
)

/** Rebuilds the aggregate from the cached row plus the already-cached base [Pokemon]. */
fun PokemonDetailEntity.toDomain(base: Pokemon, gson: Gson): PokemonDetail = PokemonDetail(
  pokemon = base,
  genus = genus,
  flavorText = flavorText,
  abilities = gson.fromJson(abilities, Array<AbilityInfo>::class.java).orEmpty().toList(),
  captureRate = captureRate,
  moves = gson.fromJson(movesJson, Array<MoveInfo>::class.java).orEmpty().toList(),
  evolution = gson.fromJson(evolutionJson, Array<EvolutionStage>::class.java).orEmpty().toList(),
  megaForms = gson.fromJson(megaJson, Array<MegaForm>::class.java).orEmpty().toList()
)

/** First English `short_effect` for an ability, whitespace-normalized; blank when none. */
fun AbilityDetailDto.englishShortEffect(): String =
  effectEntries.firstOrNull { it.language.name == ENGLISH }
    ?.shortEffect
    ?.replace(Regex("\\s+"), " ") // collapses the API's hard line breaks / form feeds
    ?.trim()
    .orEmpty()

/** Turns a PokéAPI slug like "fire-punch" into a display title "Fire Punch". */
fun String.toTitle(): String =
  split('-', ' ').joinToString(" ") { word ->
    word.replaceFirstChar { it.uppercase() }
  }

private const val ENGLISH = "en"
