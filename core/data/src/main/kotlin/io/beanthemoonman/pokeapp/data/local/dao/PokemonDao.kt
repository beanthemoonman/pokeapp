package io.beanthemoonman.pokeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Query
import io.beanthemoonman.pokeapp.data.local.entity.PokemonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {

    @Upsert
    suspend fun upsertAll(pokemon: List<PokemonEntity>)

    @Upsert
    suspend fun upsert(pokemon: PokemonEntity)

    @Query("SELECT * FROM pokemon WHERE id = :id")
    suspend fun getById(id: Int): PokemonEntity?

    @Query("SELECT * FROM pokemon ORDER BY id ASC LIMIT :limit OFFSET :offset")
    fun pageFlow(limit: Int, offset: Int): Flow<List<PokemonEntity>>

    @Query("SELECT COUNT(*) FROM pokemon")
    suspend fun count(): Int
}
