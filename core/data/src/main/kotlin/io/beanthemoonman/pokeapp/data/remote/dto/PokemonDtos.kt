package io.beanthemoonman.pokeapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Page envelope returned by `GET /pokemon?limit=&offset=`. */
data class PokemonListResponseDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<NamedApiResourceDto>
)

/** A `{ name, url }` reference. The numeric id is the last path segment of [url]. */
data class NamedApiResourceDto(
    val name: String,
    val url: String
) {
    val id: Int
        get() = url.trimEnd('/').substringAfterLast('/').toInt()
}

/** A `{ url }` reference with no name (e.g. species `evolution_chain`). */
data class ApiResourceDto(
    val url: String
) {
    val id: Int
        get() = url.trimEnd('/').substringAfterLast('/').toInt()
}

/** Detail returned by `GET /pokemon/{id}`. */
data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val sprites: SpritesDto,
    val types: List<TypeSlotDto>,
    val stats: List<StatSlotDto>,
    // The fields below are only consumed by the full-detail aggregate; the list/paging
    // path ignores them. Nullable/defaulted so older cache fills and list fetches are safe.
    val abilities: List<AbilitySlotDto> = emptyList(),
    val moves: List<MoveSlotDto> = emptyList(),
    val species: NamedApiResourceDto? = null
)

data class AbilitySlotDto(
    val ability: NamedApiResourceDto,
    @SerializedName("is_hidden") val isHidden: Boolean
)

data class MoveSlotDto(
    val move: NamedApiResourceDto,
    @SerializedName("version_group_details") val versionGroupDetails: List<VersionGroupDetailDto>
)

data class VersionGroupDetailDto(
    @SerializedName("level_learned_at") val levelLearnedAt: Int,
    @SerializedName("move_learn_method") val moveLearnMethod: NamedApiResourceDto,
    @SerializedName("version_group") val versionGroup: NamedApiResourceDto
)

/** Detail returned by `GET /move/{name}`. */
data class MoveDto(
    val id: Int,
    val name: String,
    val power: Int?,
    val accuracy: Int?,
    val pp: Int?,
    val type: NamedApiResourceDto,
    @SerializedName("damage_class") val damageClass: NamedApiResourceDto?
)

/** Detail returned by `GET /pokemon-species/{id}`. */
data class PokemonSpeciesDto(
    @SerializedName("capture_rate") val captureRate: Int,
    val genera: List<GenusDto>,
    @SerializedName("flavor_text_entries") val flavorTextEntries: List<FlavorTextEntryDto>,
    @SerializedName("evolution_chain") val evolutionChain: ApiResourceDto?
)

data class GenusDto(
    val genus: String,
    val language: NamedApiResourceDto
)

data class FlavorTextEntryDto(
    @SerializedName("flavor_text") val flavorText: String,
    val language: NamedApiResourceDto
)

/** Detail returned by `GET /evolution-chain/{id}`. */
data class EvolutionChainDto(
    val chain: ChainLinkDto
)

data class ChainLinkDto(
    val species: NamedApiResourceDto,
    @SerializedName("evolution_details") val evolutionDetails: List<EvolutionDetailDto>,
    @SerializedName("evolves_to") val evolvesTo: List<ChainLinkDto>
)

data class EvolutionDetailDto(
    @SerializedName("min_level") val minLevel: Int?,
    @SerializedName("min_happiness") val minHappiness: Int?,
    val trigger: NamedApiResourceDto?,
    val item: NamedApiResourceDto?,
    @SerializedName("held_item") val heldItem: NamedApiResourceDto?,
    @SerializedName("time_of_day") val timeOfDay: String?
)

data class SpritesDto(
    @SerializedName("front_default") val frontDefault: String?,
    val other: OtherSpritesDto?
)

data class OtherSpritesDto(
    @SerializedName("official-artwork") val officialArtwork: OfficialArtworkDto?
)

data class OfficialArtworkDto(
    @SerializedName("front_default") val frontDefault: String?
)

data class TypeSlotDto(
    val slot: Int,
    val type: NamedApiResourceDto
)

data class StatSlotDto(
    @SerializedName("base_stat") val baseStat: Int,
    val stat: NamedApiResourceDto
)
