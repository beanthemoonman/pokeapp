package io.beanthemoonman.pokeapp.phone.ui.moves

import androidx.lifecycle.SavedStateHandle
import io.beanthemoonman.pokeapp.domain.model.Move
import io.beanthemoonman.pokeapp.domain.model.MoveCategory
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.repository.MoveRepository
import io.beanthemoonman.pokeapp.domain.usecase.GetMoveDetailUseCase
import io.beanthemoonman.pokeapp.phone.ui.nav.NavDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class MoveDetailViewModelTest {

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
    fun `loads move into Success for the saved-state id`() = runTest(dispatcher) {
        val repo = FakeMoveRepository()
        val vm = newViewModel(repo, id = 53)

        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is UiState.Success)
        assertEquals(53, (state as UiState.Success).data.id)
        assertEquals(53, repo.requestedId)
    }

    @Test
    fun `failure surfaces Error and retry recovers`() = runTest(dispatcher) {
        val repo = FakeMoveRepository(throwing = true)
        val vm = newViewModel(repo, id = 53)

        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value is UiState.Error)

        repo.throwing = false
        vm.retry()
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value is UiState.Success)
    }

    private fun newViewModel(repo: FakeMoveRepository, id: Int) = MoveDetailViewModel(
        GetMoveDetailUseCase(repo),
        SavedStateHandle(mapOf(NavDestination.MoveDetail.ARG_ID to id)),
    )

    private class FakeMoveRepository(var throwing: Boolean = false) : MoveRepository {
        var requestedId: Int = -1

        override suspend fun getMoveDetail(id: Int): Move {
            requestedId = id
            if (throwing) throw RuntimeException("boom")
            return Move(
                id = id,
                name = "Flamethrower",
                type = Type.FIRE,
                category = MoveCategory.SPECIAL,
                power = 90,
                accuracy = 100,
                pp = 15,
                generationId = 1,
                shortEffect = "Has a 10% chance to burn the target.",
            )
        }

        override suspend fun getMovePage(generationId: Int, offset: Int, count: Int): List<Move> = emptyList()
        override suspend fun searchMoves(generationId: Int, query: String): List<Move> = emptyList()
    }
}
