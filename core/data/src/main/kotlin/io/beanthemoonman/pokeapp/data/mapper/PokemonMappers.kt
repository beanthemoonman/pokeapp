package io.beanthemoonman.pokeapp.data.mapper

import io.beanthemoonman.pokeapp.data.local.entity.PokemonEntity
import io.beanthemoonman.pokeapp.data.remote.dto.PokemonDetailDto
import io.beanthemoonman.pokeapp.domain.model.Pokemon
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
