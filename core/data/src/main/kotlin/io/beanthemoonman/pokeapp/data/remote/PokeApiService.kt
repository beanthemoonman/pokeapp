package io.beanthemoonman.pokeapp.data.remote

import io.beanthemoonman.pokeapp.data.remote.dto.AbilityDetailDto
import io.beanthemoonman.pokeapp.data.remote.dto.EvolutionChainDto
import io.beanthemoonman.pokeapp.data.remote.dto.GenerationDto
import io.beanthemoonman.pokeapp.data.remote.dto.ItemDetailDto
import io.beanthemoonman.pokeapp.data.remote.dto.ItemListResponseDto
import io.beanthemoonman.pokeapp.data.remote.dto.MoveDetailDto
import io.beanthemoonman.pokeapp.data.remote.dto.MoveDto
import io.beanthemoonman.pokeapp.data.remote.dto.PokemonDetailDto
import io.beanthemoonman.pokeapp.data.remote.dto.PokemonListResponseDto
import io.beanthemoonman.pokeapp.data.remote.dto.PokemonSpeciesDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Retrofit binding for PokéAPI v2 (https://pokeapi.co/api/v2/). No auth required. */
interface PokeApiService {

  @GET("pokemon")
  suspend fun getPokemonList(
    @Query("limit") limit: Int,
    @Query("offset") offset: Int
  ): PokemonListResponseDto

  @GET("pokemon/{id}")
  suspend fun getPokemonDetail(@Path("id") id: Int): PokemonDetailDto

  @GET("pokemon-species/{id}")
  suspend fun getPokemonSpecies(@Path("id") id: Int): PokemonSpeciesDto

  @GET("evolution-chain/{id}")
  suspend fun getEvolutionChain(@Path("id") id: Int): EvolutionChainDto

  @GET("move/{name}")
  suspend fun getMove(@Path("name") name: String): MoveDto

  @GET("ability/{name}")
  suspend fun getAbility(@Path("name") name: String): AbilityDetailDto

  @GET("move/{id}")
  suspend fun getMoveDetail(@Path("id") id: Int): MoveDetailDto

  @GET("generation/{id}")
  suspend fun getGeneration(@Path("id") id: Int): GenerationDto

  @GET("item")
  suspend fun getItemList(
    @Query("limit") limit: Int,
    @Query("offset") offset: Int
  ): ItemListResponseDto

  @GET("item/{id}")
  suspend fun getItem(@Path("id") id: Int): ItemDetailDto

  companion object {
    const val BASE_URL = "https://pokeapi.co/api/v2/"
  }
}
