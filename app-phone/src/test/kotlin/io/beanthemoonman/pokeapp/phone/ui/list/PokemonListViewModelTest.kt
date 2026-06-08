package io.beanthemoonman.pokeapp.phone.ui.list

import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.model.Stats
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.repository.GenerationRepository
import io.beanthemoonman.pokeapp.domain.repository.PokemonRepository
import io.beanthemoonman.pokeapp.domain.usecase.GetPokemonListUseCase
import io.beanthemoonman.pokeapp.domain.usecase.ObserveSelectedGenerationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
class PokemonListViewModelTest {

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
    fun `emits Success when repository returns pokemon`() = runTest(dispatcher) {
        val vm = newViewModel(FakeRepository(listOf(bulbasaur)))

        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is UiState.Success)
        assertEquals(listOf(bulbasaur), (state as UiState.Success).data)
    }

    @Test
    fun `emits Error when repository returns an empty page`() = runTest(dispatcher) {
        val vm = newViewModel(FakeRepository(emptyList()))

        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state.value is UiState.Error)
    }

    @Test
    fun `emits Error when the repository flow throws`() = runTest(dispatcher) {
        val vm = newViewModel(FakeRepository(throwing = true))

        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state.value is UiState.Error)
    }

    /** Builds the VM with a generation already selected (Gen I), so the list pipeline runs. */
    private fun newViewModel(repo: FakeRepository) = PokemonListViewModel(
        GetPokemonListUseCase(repo),
        ObserveSelectedGenerationUseCase(FakeGenerationRepository(selectedId = 1)),
    )

    private class FakeRepository(
        private val data: List<Pokemon> = emptyList(),
        private val throwing: Boolean = false,
    ) : PokemonRepository {
        override fun getPokemonList(limit: Int, offset: Int): Flow<List<Pokemon>> = flow {
            if (throwing) throw RuntimeException("boom")
            emit(data)
        }

        override suspend fun getPokemonDetail(id: Int): Pokemon = bulbasaur
        override fun getTeam(): Flow<List<Pokemon?>> = flow { emit(emptyList()) }
        override suspend fun setTeamSlot(slot: Int, pokemonId: Int?) = Unit
    }

    private class FakeGenerationRepository(private val selectedId: Int?) : GenerationRepository {
        override val selectedGenerationId: Flow<Int?> = flowOf(selectedId)
        override suspend fun selectGeneration(id: Int) = Unit
    }

    private companion object {
        val bulbasaur = Pokemon(
            id = 1,
            name = "Bulbasaur",
            spriteUrl = "https://example.com/1.png",
            types = listOf(Type.GRASS, Type.POISON),
            stats = Stats(45, 49, 49, 65, 65, 45),
            height = 7,
            weight = 69,
        )
    }
}
