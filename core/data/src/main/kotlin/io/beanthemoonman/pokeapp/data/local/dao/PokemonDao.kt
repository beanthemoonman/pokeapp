package io.beanthemoonman.pokeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.beanthemoonman.pokeapp.data.local.entity.PokemonEntity

@Dao
interface PokemonDao {

  @Upsert
  suspend fun upsertAll(pokemon: List<PokemonEntity>)

  @Upsert
  suspend fun upsert(pokemon: PokemonEntity)

  @Query("SELECT * FROM pokemon WHERE id = :id")
  suspend fun getById(id: Int): PokemonEntity?

  @Query("SELECT * FROM pokemon WHERE id IN (:ids) ORDER BY id ASC")
  suspend fun getByIds(ids: List<Int>): List<PokemonEntity>
}
