package io.beanthemoonman.pokeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.beanthemoonman.pokeapp.data.local.entity.PokemonDetailEntity

@Dao
interface PokemonDetailDao {

    @Upsert
    suspend fun upsert(detail: PokemonDetailEntity)

    @Query("SELECT * FROM pokemon_detail WHERE id = :id")
    suspend fun getById(id: Int): PokemonDetailEntity?
}
