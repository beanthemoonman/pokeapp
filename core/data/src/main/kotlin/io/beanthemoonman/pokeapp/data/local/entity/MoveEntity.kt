package io.beanthemoonman.pokeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Flat cache row for a dictionary move. [type] stores the [io.beanthemoonman.pokeapp.domain.model.Type]
 * enum name (or null for non-battle types) and [category] the damage-class slug, both
 * resolved at map time — no nested objects or converters needed.
 */
@Entity(tableName = "move")
data class MoveEntity(
  @PrimaryKey val id: Int,
  val name: String,
  /** [io.beanthemoonman.pokeapp.domain.model.Type] enum name, or null. */
  val type: String?,
  /** [io.beanthemoonman.pokeapp.domain.model.MoveCategory] name. */
  val category: String,
  val power: Int?,
  val accuracy: Int?,
  val pp: Int,
  val generationId: Int,
  val shortEffect: String,
  val cachedAt: Long
)
