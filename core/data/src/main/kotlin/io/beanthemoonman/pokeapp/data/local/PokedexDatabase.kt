package io.beanthemoonman.pokeapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.beanthemoonman.pokeapp.data.local.dao.PokemonDao
import io.beanthemoonman.pokeapp.data.local.dao.PokemonDetailDao
import io.beanthemoonman.pokeapp.data.local.dao.TeamDao
import io.beanthemoonman.pokeapp.data.local.entity.PokemonDetailEntity
import io.beanthemoonman.pokeapp.data.local.entity.PokemonEntity
import io.beanthemoonman.pokeapp.data.local.entity.TeamEntity
import io.beanthemoonman.pokeapp.data.local.entity.TypeEntity

@Database(
    entities = [PokemonEntity::class, PokemonDetailEntity::class, TypeEntity::class, TeamEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PokedexDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
    abstract fun pokemonDetailDao(): PokemonDetailDao
    abstract fun teamDao(): TeamDao

    companion object {
        const val NAME = "pokedex.db"
    }
}
