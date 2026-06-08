package io.beanthemoonman.pokeapp.phone.ui.detail

import androidx.lifecycle.SavedStateHandle
import io.beanthemoonman.pokeapp.domain.model.EvolutionStage
import io.beanthemoonman.pokeapp.domain.model.MoveCategory
import io.beanthemoonman.pokeapp.domain.model.MoveInfo
import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.model.PokemonDetail
import io.beanthemoonman.pokeapp.domain.model.Stats
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.repository.PokemonRepository
import io.beanthemoonman.pokeapp.domain.usecase.GetPokemonDetailFullUseCase
import io.beanthemoonman.pokeapp.phone.ui.nav.NavDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads detail into Success for the saved-state id`() = runTest(dispatcher) {
        val repo = FakeRepository()
        val vm = newViewModel(repo, id = 6)

        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is UiState.Success)
        val detail = (state as UiState.Success).data
        assertEquals(6, detail.pokemon.id)
        assertEquals(1, detail.moves.size)
        assertEquals(2, detail.evolution.size)
        assertEquals(6, repo.requestedId)
    }

    @Test
    fun `failure surfaces Error and retry recovers`() = runTest(dispatcher) {
        val repo = FakeRepository(throwing = true)
        val vm = newViewModel(repo, id = 6)

        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value is UiState.Error)

        repo.throwing = false
        vm.retry()
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value is UiState.Success)
    }

    private fun newViewModel(repo: FakeRepository, id: Int) = PokemonDetailViewModel(
        GetPokemonDetailFullUseCase(repo),
        SavedStateHandle(mapOf(NavDestination.Detail.ARG_ID to id)),
    )

    private class FakeRepository(var throwing: Boolean = false) : PokemonRepository {
        var requestedId: Int = -1

        override suspend fun getPokemonDetailFull(id: Int): PokemonDetail {
            requestedId = id
            if (throwing) throw RuntimeException("boom")
            return PokemonDetail(
                pokemon = pokemon(id),
                genus = "Flame Pokémon",
                flavorText = "Spits fire.",
                abilities = listOf("Blaze"),
                captureRate = 45,
                moves = listOf(
                    MoveInfo("Flamethrower", Type.FIRE, MoveCategory.SPECIAL, 90, 100, 15, level = 36),
                ),
                evolution = listOf(
                    EvolutionStage(4, "Charmander", "", listOf(Type.FIRE), null),
                    EvolutionStage(id, "Charizard", "", listOf(Type.FIRE), "Lv. 36"),
                ),
            )
        }

        override suspend fun getPokemonPage(startId: Int, count: Int): List<Pokemon> = emptyList()
        override suspend fun searchPokemon(query: String, dexEnd: Int): List<Pokemon> = emptyList()
        override suspend fun getPokemonDetail(id: Int): Pokemon = pokemon(id)
        override fun getTeam(): Flow<List<Pokemon?>> = flow { emit(emptyList()) }
        override suspend fun setTeamSlot(slot: Int, pokemonId: Int?) = Unit
    }

    private companion object {
        fun pokemon(id: Int) = Pokemon(
            id = id,
            name = "Pokemon$id",
            spriteUrl = "https://example.com/$id.png",
            types = listOf(Type.FIRE),
            stats = Stats(78, 84, 78, 109, 85, 100),
            height = 17,
            weight = 905,
        )
    }
}
