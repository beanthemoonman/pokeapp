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

/** Detail returned by `GET /pokemon/{id}`. */
data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val sprites: SpritesDto,
    val types: List<TypeSlotDto>,
    val stats: List<StatSlotDto>
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
