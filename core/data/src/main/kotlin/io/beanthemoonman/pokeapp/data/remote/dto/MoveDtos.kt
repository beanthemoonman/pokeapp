package io.beanthemoonman.pokeapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Subset of `GET /generation/{id}` — the moves introduced in that generation, as
 * `{ name, url }` refs. The dictionary for "through gen X" is the union of these
 * lists for generations `1..X`.
 */
data class GenerationDto(
    val id: Int,
    val moves: List<NamedApiResourceDto> = emptyList()
)

/**
 * Detail returned by `GET /move/{id}` for the dictionary. Distinct from [MoveDto]
 * (the Pokémon detail "Moves" tab shape) — this one also needs the introduction
 * [generation] and the English [effectEntries].
 */
data class MoveDetailDto(
    val id: Int,
    val name: String,
    val power: Int?,
    val accuracy: Int?,
    val pp: Int?,
    val type: NamedApiResourceDto,
    @SerializedName("damage_class") val damageClass: NamedApiResourceDto?,
    val generation: NamedApiResourceDto,
    @SerializedName("effect_entries") val effectEntries: List<MoveEffectEntryDto> = emptyList()
)

data class MoveEffectEntryDto(
    val effect: String,
    @SerializedName("short_effect") val shortEffect: String,
    val language: NamedApiResourceDto
)
