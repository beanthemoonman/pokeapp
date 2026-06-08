package io.beanthemoonman.pokeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Flat cache row for a dictionary item. [category] stores the [ItemCategory] slug
 * (resolved at map time), so no nested objects or converters are needed.
 */
@Entity(tableName = "item")
data class ItemEntity(
    @PrimaryKey val id: Int,
    val name: String,
    /** [io.beanthemoonman.pokeapp.domain.model.ItemCategory] slug. */
    val category: String,
    val cost: Int,
    val shortEffect: String,
    val flavor: String,
    val spriteUrl: String,
    val cachedAt: Long
)
