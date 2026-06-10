package io.beanthemoonman.pokeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "type")
data class TypeEntity(
  @PrimaryKey val id: Int,
  val name: String
)
