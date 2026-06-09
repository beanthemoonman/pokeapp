package io.beanthemoonman.pokeapp.uistate.typecalc

import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.repository.GenerationRepository
import io.beanthemoonman.pokeapp.domain.type.DefenseBucket
import io.beanthemoonman.pokeapp.domain.usecase.GroupDefenseEffectivenessUseCase
import io.beanthemoonman.pokeapp.domain.usecase.ObserveSelectedGenerationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TypeMatchupViewModelTest {

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
    fun `starts empty with no defender picked`() = runTest(dispatcher) {
        val vm = newViewModel(generationId = 9)
        val job = launch { vm.state.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is UiState.Success)
        val data = (state as UiState.Success).data
        assertEquals(18, data.roster.size)
        assertTrue(data.defenders.isEmpty())
        assertFalse(data.defending)

        job.cancel()
    }

    @Test
    fun `gen one roster excludes later types`() = runTest(dispatcher) {
        val vm = newViewModel(generationId = 1)
        val job = launch { vm.state.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        val data = (vm.state.value as UiState.Success).data
        assertEquals(15, data.roster.size)
        assertFalse(data.roster.contains(Type.FAIRY))
        assertFalse(data.roster.contains(Type.STEEL))
        assertFalse(data.roster.contains(Type.DARK))

        job.cancel()
    }

    @Test
    fun `dual defender combo stacks into x4 and x0 buckets`() = runTest(dispatcher) {
        val vm = newViewModel(generationId = 9)
        val job = launch { vm.state.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        // Charizard's Fire/Flying: Rock hits ×4; Ground does nothing (×0).
        vm.toggleDefender(Type.FIRE)
        vm.toggleDefender(Type.FLYING)
        dispatcher.scheduler.advanceUntilIdle()

        val data = (vm.state.value as UiState.Success).data
        assertTrue(data.defending)
        assertEquals(listOf(Type.FIRE, Type.FLYING), data.defenders)
        assertTrue(data.defenseGroups[DefenseBucket.QUAD].orEmpty().contains(Type.ROCK))
        assertTrue(data.defenseGroups[DefenseBucket.IMMUNE].orEmpty().contains(Type.GROUND))

        job.cancel()
    }

    @Test
    fun `defender selection is capped at two by evicting the oldest`() = runTest(dispatcher) {
        val vm = newViewModel(generationId = 9)
        val job = launch { vm.state.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        vm.toggleDefender(Type.FIRE)
        vm.toggleDefender(Type.FLYING)
        vm.toggleDefender(Type.WATER) // evicts FIRE
        dispatcher.scheduler.advanceUntilIdle()

        var data = (vm.state.value as UiState.Success).data
        assertEquals(listOf(Type.FLYING, Type.WATER), data.defenders)

        vm.toggleDefender(Type.FLYING) // toggling a selected type removes it
        vm.clearDefenders()
        dispatcher.scheduler.advanceUntilIdle()

        data = (vm.state.value as UiState.Success).data
        assertTrue(data.defenders.isEmpty())

        job.cancel()
    }

    private fun newViewModel(generationId: Int) = TypeMatchupViewModel(
        ObserveSelectedGenerationUseCase(FakeGenerationRepository(generationId)),
        GroupDefenseEffectivenessUseCase(),
    )

    private class FakeGenerationRepository(id: Int?) : GenerationRepository {
        override val selectedGenerationId: Flow<Int?> = MutableStateFlow(id)
        override suspend fun selectGeneration(id: Int) = Unit
    }
}
