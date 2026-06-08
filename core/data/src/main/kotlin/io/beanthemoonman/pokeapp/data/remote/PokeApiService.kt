package io.beanthemoonman.pokeapp.data.remote

import io.beanthemoonman.pokeapp.data.remote.dto.EvolutionChainDto
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

    companion object {
        const val BASE_URL = "https://pokeapi.co/api/v2/"
    }
}
