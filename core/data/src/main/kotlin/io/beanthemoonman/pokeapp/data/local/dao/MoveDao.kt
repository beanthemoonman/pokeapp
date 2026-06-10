package io.beanthemoonman.pokeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.beanthemoonman.pokeapp.data.local.entity.MoveEntity

@Dao
interface MoveDao {

  @Upsert
  suspend fun upsertAll(moves: List<MoveEntity>)

  @Upsert
  suspend fun upsert(move: MoveEntity)

  @Query("SELECT * FROM move WHERE id = :id")
  suspend fun getById(id: Int): MoveEntity?

  @Query("SELECT * FROM move WHERE id IN (:ids) ORDER BY id ASC")
  suspend fun getByIds(ids: List<Int>): List<MoveEntity>
}
