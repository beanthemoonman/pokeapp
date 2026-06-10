package io.beanthemoonman.pokeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.beanthemoonman.pokeapp.domain.model.Type

/**
 * Flat cache row for a Pokémon. Lists are serialized via TypeConverters; no nested
 * objects are stored (per the flat-entity rule).
 */
@Entity(tableName = "pokemon")
data class PokemonEntity(
  @PrimaryKey val id: Int,
  val name: String,
  val spriteUrl: String,
  val types: List<Type>,
  /** Base stats in fixed order: hp, attack, defense, specialAttack, specialDefense, speed. */
  val stats: List<Int>,
  val height: Int,
  val weight: Int,
  val cachedAt: Long
)
