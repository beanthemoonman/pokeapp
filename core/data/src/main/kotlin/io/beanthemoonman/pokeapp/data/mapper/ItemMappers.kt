package io.beanthemoonman.pokeapp.data.mapper

import io.beanthemoonman.pokeapp.data.local.entity.ItemEntity
import io.beanthemoonman.pokeapp.data.remote.dto.ItemDetailDto
import io.beanthemoonman.pokeapp.domain.model.Item
import io.beanthemoonman.pokeapp.domain.model.ItemCategory

fun ItemDetailDto.toEntity(cachedAt: Long): ItemEntity = ItemEntity(
    id = id,
    name = name.toTitle(),
    category = ItemCategory.fromApiName(category.name).slug,
    cost = cost,
    shortEffect = effectEntries.englishShortEffect(),
    flavor = flavorTextEntries.englishText(),
    spriteUrl = sprites?.default.orEmpty(),
    cachedAt = cachedAt
)

fun ItemEntity.toDomain(): Item = Item(
    id = id,
    name = name,
    category = ItemCategory.fromSlug(category),
    cost = cost,
    shortEffect = shortEffect,
    flavor = flavor,
    spriteUrl = spriteUrl
)

/** First English `short_effect`, whitespace-normalized; blank when none. */
private fun List<io.beanthemoonman.pokeapp.data.remote.dto.ItemEffectEntryDto>.englishShortEffect(): String =
    firstOrNull { it.language.name == ENGLISH }?.shortEffect?.cleanText().orEmpty()

/** First English flavor text, whitespace-normalized; blank when none. */
private fun List<io.beanthemoonman.pokeapp.data.remote.dto.ItemFlavorTextEntryDto>.englishText(): String =
    firstOrNull { it.language.name == ENGLISH }?.text?.cleanText().orEmpty()

/** Collapses the API's hard line breaks / form feeds into single spaces. */
private fun String.cleanText(): String =
    replace('\n', ' ').replace('', ' ').replace(Regex("\\s+"), " ").trim()

private const val ENGLISH = "en"
