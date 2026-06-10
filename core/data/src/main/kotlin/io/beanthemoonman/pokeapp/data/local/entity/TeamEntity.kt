package io.beanthemoonman.pokeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One of six team slots (0–5). A null [pokemonId] means the slot is empty. */
@Entity(tableName = "team")
data class TeamEntity(
  @PrimaryKey val teamSlot: Int,
  val pokemonId: Int?
)
