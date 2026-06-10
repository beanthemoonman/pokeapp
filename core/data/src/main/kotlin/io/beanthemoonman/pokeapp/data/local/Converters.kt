package io.beanthemoonman.pokeapp.data.local

import androidx.room.TypeConverter
import io.beanthemoonman.pokeapp.domain.model.Type

/** Serializes the list fields on [io.beanthemoonman.pokeapp.data.local.entity.PokemonEntity]. */
class Converters {

  @TypeConverter
  fun typesToString(types: List<Type>): String =
    types.joinToString(SEPARATOR) { it.name }

  @TypeConverter
  fun stringToTypes(value: String): List<Type> =
    if (value.isEmpty()) emptyList()
    else value.split(SEPARATOR).map { Type.valueOf(it) }

  @TypeConverter
  fun statsToString(stats: List<Int>): String =
    stats.joinToString(SEPARATOR)

  @TypeConverter
  fun stringToStats(value: String): List<Int> =
    if (value.isEmpty()) emptyList()
    else value.split(SEPARATOR).map { it.toInt() }

  private companion object {
    const val SEPARATOR = ","
  }
}
