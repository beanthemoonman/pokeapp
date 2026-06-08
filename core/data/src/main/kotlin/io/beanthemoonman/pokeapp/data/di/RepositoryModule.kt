package io.beanthemoonman.pokeapp.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.beanthemoonman.pokeapp.data.repository.GenerationRepositoryImpl
import io.beanthemoonman.pokeapp.data.repository.PokemonRepositoryImpl
import io.beanthemoonman.pokeapp.domain.repository.GenerationRepository
import io.beanthemoonman.pokeapp.domain.repository.PokemonRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPokemonRepository(impl: PokemonRepositoryImpl): PokemonRepository

    @Binds
    @Singleton
    abstract fun bindGenerationRepository(impl: GenerationRepositoryImpl): GenerationRepository
}
