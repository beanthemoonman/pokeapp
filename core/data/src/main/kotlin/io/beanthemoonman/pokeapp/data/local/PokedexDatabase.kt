package io.beanthemoonman.pokeapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.beanthemoonman.pokeapp.data.local.dao.ItemDao
import io.beanthemoonman.pokeapp.data.local.dao.MoveDao
import io.beanthemoonman.pokeapp.data.local.dao.PokemonDao
import io.beanthemoonman.pokeapp.data.local.dao.PokemonDetailDao
import io.beanthemoonman.pokeapp.data.local.dao.TeamDao
import io.beanthemoonman.pokeapp.data.local.entity.ItemEntity
import io.beanthemoonman.pokeapp.data.local.entity.MoveEntity
import io.beanthemoonman.pokeapp.data.local.entity.PokemonDetailEntity
import io.beanthemoonman.pokeapp.data.local.entity.PokemonEntity
import io.beanthemoonman.pokeapp.data.local.entity.TeamEntity
import io.beanthemoonman.pokeapp.data.local.entity.TypeEntity

@Database(
    entities = [PokemonEntity::class, PokemonDetailEntity::class, TypeEntity::class, TeamEntity::class, ItemEntity::class, MoveEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PokedexDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
    abstract fun pokemonDetailDao(): PokemonDetailDao
    abstract fun teamDao(): TeamDao
    abstract fun itemDao(): ItemDao
    abstract fun moveDao(): MoveDao

    companion object {
        const val NAME = "pokedex.db"
    }
}
