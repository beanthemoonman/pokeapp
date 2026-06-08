package io.beanthemoonman.pokeapp.data.mapper

import io.beanthemoonman.pokeapp.data.local.entity.MoveEntity
import io.beanthemoonman.pokeapp.data.remote.dto.MoveDetailDto
import io.beanthemoonman.pokeapp.data.remote.dto.MoveEffectEntryDto
import io.beanthemoonman.pokeapp.domain.model.Move
import io.beanthemoonman.pokeapp.domain.model.MoveCategory
import io.beanthemoonman.pokeapp.domain.model.Type

fun MoveDetailDto.toEntity(cachedAt: Long): MoveEntity = MoveEntity(
    id = id,
    name = name.toTitle(),
    type = Type.fromApiName(type.name)?.name,
    category = damageClass.toCategory().name,
    power = power,
    accuracy = accuracy,
    pp = pp ?: 0,
    generationId = generation.name.toGenerationId(),
    shortEffect = effectEntries.englishShortEffect(),
    cachedAt = cachedAt
)

fun MoveEntity.toDomain(): Move = Move(
    id = id,
    name = name,
    type = type?.let { runCatching { Type.valueOf(it) }.getOrNull() },
    category = runCatching { MoveCategory.valueOf(category) }.getOrDefault(MoveCategory.STATUS),
    power = power,
    accuracy = accuracy,
    pp = pp,
    generationId = generationId,
    shortEffect = shortEffect
)

private fun io.beanthemoonman.pokeapp.data.remote.dto.NamedApiResourceDto?.toCategory(): MoveCategory =
    when (this?.name) {
        "physical" -> MoveCategory.PHYSICAL
        "special" -> MoveCategory.SPECIAL
        else -> MoveCategory.STATUS
    }

/** First English `short_effect`, whitespace-normalized; blank when none. */
private fun List<MoveEffectEntryDto>.englishShortEffect(): String =
    firstOrNull { it.language.name == ENGLISH }?.shortEffect?.cleanText().orEmpty()

/** Collapses the API's hard line breaks / form feeds into single spaces. */
private fun String.cleanText(): String =
    replace('\n', ' ').replace('', ' ').replace(Regex("\\s+"), " ").trim()

/** Maps a PokéAPI generation slug ("generation-i" … "generation-ix") to its id (1..9). */
private fun String.toGenerationId(): Int =
    ROMAN_BY_SLUG[substringAfterLast('-')] ?: 0

private val ROMAN_BY_SLUG = mapOf(
    "i" to 1, "ii" to 2, "iii" to 3, "iv" to 4, "v" to 5,
    "vi" to 6, "vii" to 7, "viii" to 8, "ix" to 9,
)

private const val ENGLISH = "en"
