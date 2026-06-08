package io.beanthemoonman.pokeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Flat cache row for the rich detail aggregate (species text, moves, evolution line).
 * The base stats/types live in [PokemonEntity]; this row holds only the extra detail-tab
 * data. [abilities] is comma-joined; [movesJson]/[evolutionJson] are Gson-serialized in
 * the repository (kept as opaque String columns so the entity stays flat).
 */
@Entity(tableName = "pokemon_detail")
data class PokemonDetailEntity(
    @PrimaryKey val id: Int,
    val genus: String,
    val flavorText: String,
    val abilities: String,
    val captureRate: Int,
    val movesJson: String,
    val evolutionJson: String,
    val cachedAt: Long
)
