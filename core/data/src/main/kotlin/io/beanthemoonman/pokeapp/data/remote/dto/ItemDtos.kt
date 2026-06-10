package io.beanthemoonman.pokeapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Page envelope returned by `GET /item?limit=&offset=`. */
data class ItemListResponseDto(
  val count: Int,
  val next: String?,
  val previous: String?,
  val results: List<NamedApiResourceDto>
)

/** Detail returned by `GET /item/{id}`. */
data class ItemDetailDto(
  val id: Int,
  val name: String,
  val cost: Int,
  val category: NamedApiResourceDto,
  @SerializedName("effect_entries") val effectEntries: List<ItemEffectEntryDto> = emptyList(),
  @SerializedName("flavor_text_entries") val flavorTextEntries: List<ItemFlavorTextEntryDto> = emptyList(),
  val sprites: ItemSpritesDto? = null
)

data class ItemEffectEntryDto(
  val effect: String,
  @SerializedName("short_effect") val shortEffect: String,
  val language: NamedApiResourceDto
)

data class ItemFlavorTextEntryDto(
  val text: String,
  val language: NamedApiResourceDto,
  @SerializedName("version_group") val versionGroup: NamedApiResourceDto? = null
)

data class ItemSpritesDto(
  val default: String?
)
