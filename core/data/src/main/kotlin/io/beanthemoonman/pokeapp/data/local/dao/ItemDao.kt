package io.beanthemoonman.pokeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.beanthemoonman.pokeapp.data.local.entity.ItemEntity

@Dao
interface ItemDao {

  @Upsert
  suspend fun upsertAll(items: List<ItemEntity>)

  @Upsert
  suspend fun upsert(item: ItemEntity)

  @Query("SELECT * FROM item WHERE id = :id")
  suspend fun getById(id: Int): ItemEntity?

  @Query("SELECT * FROM item WHERE id IN (:ids) ORDER BY id ASC")
  suspend fun getByIds(ids: List<Int>): List<ItemEntity>
}
