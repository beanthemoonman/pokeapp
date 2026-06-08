package io.beanthemoonman.pokeapp.data.remote

import io.beanthemoonman.pokeapp.data.remote.dto.PokemonDetailDto
import io.beanthemoonman.pokeapp.data.remote.dto.PokemonListResponseDto
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

    companion object {
        const val BASE_URL = "https://pokeapi.co/api/v2/"
    }
}
