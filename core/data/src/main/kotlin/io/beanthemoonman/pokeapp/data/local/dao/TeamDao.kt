package io.beanthemoonman.pokeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.beanthemoonman.pokeapp.data.local.entity.TeamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {

  @Query("SELECT * FROM team ORDER BY teamSlot ASC")
  fun getTeamFlow(): Flow<List<TeamEntity>>

  @Upsert
  suspend fun setSlot(slot: TeamEntity)
}
