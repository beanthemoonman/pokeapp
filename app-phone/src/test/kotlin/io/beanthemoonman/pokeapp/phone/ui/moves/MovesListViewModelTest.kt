package io.beanthemoonman.pokeapp.phone.ui.moves

import io.beanthemoonman.pokeapp.domain.model.Move
import io.beanthemoonman.pokeapp.domain.model.MoveCategory
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.repository.GenerationRepository
import io.beanthemoonman.pokeapp.domain.repository.MoveRepository
import io.beanthemoonman.pokeapp.domain.usecase.GetMovePageUseCase
import io.beanthemoonman.pokeapp.domain.usecase.ObserveSelectedGenerationUseCase
import io.beanthemoonman.pokeapp.domain.usecase.SearchMovesUseCase
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
class MovesListViewModelTest {

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
    fun `first page loads into Success for the selected generation`() = runTest(dispatcher) {
        val repo = FakeMoveRepository(total = 100)
        val vm = newViewModel(repo, selectedGen = 1)
        dispatcher.scheduler.advanceUntilIdle()

        val data = (vm.state.value as UiState.Success).data
        assertEquals(GetMovePageUseCase.PAGE_SIZE, data.moves.size)
        assertEquals(1, data.moves.first().id)
        assertEquals(1, repo.lastGenerationId)
        assertFalse(data.endReached)
    }

    @Test
    fun `first page failure surfaces Error`() = runTest(dispatcher) {
        val vm = newViewModel(FakeMoveRepository(total = 100, throwing = true), selectedGen = 1)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state.value is UiState.Error)
    }

    @Test
    fun `loadMore appends the next page`() = runTest(dispatcher) {
        val vm = newViewModel(FakeMoveRepository(total = 100), selectedGen = 1)
        dispatcher.scheduler.advanceUntilIdle()

        vm.loadMore()
        dispatcher.scheduler.advanceUntilIdle()

        val data = (vm.state.value as UiState.Success).data
        assertEquals(GetMovePageUseCase.PAGE_SIZE * 2, data.moves.size)
    }

    @Test
    fun `paging stops at the end of the dictionary`() = runTest(dispatcher) {
        val vm = newViewModel(FakeMoveRepository(total = 40), selectedGen = 1)
        dispatcher.scheduler.advanceUntilIdle()

        vm.loadMore()
        dispatcher.scheduler.advanceUntilIdle()

        val data = (vm.state.value as UiState.Success).data
        assertEquals(40, data.moves.size)
        assertTrue(data.endReached)
    }

    @Test
    fun `changing generation reloads the dictionary`() = runTest(dispatcher) {
        val repo = FakeMoveRepository(total = 100)
        val genRepo = FakeGenerationRepository(selectedId = 1)
        val vm = MovesListViewModel(
            GetMovePageUseCase(repo),
            SearchMovesUseCase(repo),
            ObserveSelectedGenerationUseCase(genRepo),
        )
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, repo.lastGenerationId)

        genRepo.selectGeneration(5)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(5, repo.lastGenerationId)
        assertTrue(vm.state.value is UiState.Success)
    }

    @Test
    fun `selecting a class filters the visible list`() = runTest(dispatcher) {
        val vm = newViewModel(FakeMoveRepository(total = 30), selectedGen = 1)
        dispatcher.scheduler.advanceUntilIdle()

        vm.selectCategory(MoveCategory.PHYSICAL)
        val data = (vm.state.value as UiState.Success).data
        assertEquals(MoveCategory.PHYSICAL, data.category)
        assertTrue(data.visible.all { it.category == MoveCategory.PHYSICAL })
        assertEquals(30, data.moves.size)

        vm.selectCategory(null)
        assertEquals(30, (vm.state.value as UiState.Success).data.visible.size)
    }

    @Test
    fun `blank query keeps search Idle`() = runTest(dispatcher) {
        val vm = newViewModel(FakeMoveRepository(total = 30), selectedGen = 1)
        backgroundScope.launch { vm.searchState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.searchState.value is MovesSearchUiState.Idle)
    }

    @Test
    fun `name query yields Results`() = runTest(dispatcher) {
        val vm = newViewModel(FakeMoveRepository(total = 30), selectedGen = 1)
        backgroundScope.launch { vm.searchState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        vm.onQueryChange("Move1")
        dispatcher.scheduler.advanceUntilIdle()

        val s = vm.searchState.value
        assertTrue(s is MovesSearchUiState.Results)
        assertTrue((s as MovesSearchUiState.Results).moves.all { it.name.contains("Move1") })
    }

    @Test
    fun `unmatched query yields Empty`() = runTest(dispatcher) {
        val vm = newViewModel(FakeMoveRepository(total = 30), selectedGen = 1)
        backgroundScope.launch { vm.searchState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        vm.onQueryChange("zzz-nope")
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.searchState.value is MovesSearchUiState.Empty)
    }

    @Test
    fun `search failure surfaces Error and retry recovers`() = runTest(dispatcher) {
        val repo = FakeMoveRepository(total = 30, searchThrowing = true)
        val vm = newViewModel(repo, selectedGen = 1)
        backgroundScope.launch { vm.searchState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        vm.onQueryChange("Move1")
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.searchState.value is MovesSearchUiState.Error)

        repo.failSearch = false
        vm.retrySearch()
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.searchState.value is MovesSearchUiState.Results)
    }

    private fun newViewModel(repo: FakeMoveRepository, selectedGen: Int?) = MovesListViewModel(
        GetMovePageUseCase(repo),
        SearchMovesUseCase(repo),
        ObserveSelectedGenerationUseCase(FakeGenerationRepository(selectedId = selectedGen)),
    )

    private class FakeMoveRepository(
        private val total: Int,
        private val throwing: Boolean = false,
        searchThrowing: Boolean = false,
    ) : MoveRepository {
        var failSearch: Boolean = searchThrowing
        var lastGenerationId: Int = -1

        override suspend fun getMovePage(generationId: Int, offset: Int, count: Int): List<Move> {
            lastGenerationId = generationId
            if (throwing) throw RuntimeException("boom")
            val startId = offset + 1
            val end = minOf(offset + count, total)
            if (startId > end) return emptyList()
            return (startId..end).map { move(it) }
        }

        override suspend fun searchMoves(generationId: Int, query: String): List<Move> {
            lastGenerationId = generationId
            if (failSearch) throw RuntimeException("search boom")
            val q = query.trim()
            if (q.isEmpty()) return emptyList()
            q.toIntOrNull()?.let { id ->
                return if (id in 1..total) listOf(move(id)) else emptyList()
            }
            return (1..total).map { move(it) }.filter { it.name.contains(q, ignoreCase = true) }
        }

        override suspend fun getMoveDetail(id: Int): Move = move(id)
    }

    private class FakeGenerationRepository(selectedId: Int?) : GenerationRepository {
        private val flow = MutableStateFlow(selectedId)
        override val selectedGenerationId: Flow<Int?> = flow
        override suspend fun selectGeneration(id: Int) {
            flow.value = id
        }
    }

    private companion object {
        /** Alternates damage classes so class-filter tests have a non-trivial split. */
        fun move(id: Int) = Move(
            id = id,
            name = "Move$id",
            type = Type.NORMAL,
            category = if (id % 2 == 0) MoveCategory.PHYSICAL else MoveCategory.SPECIAL,
            power = if (id % 3 == 0) null else id,
            accuracy = 100,
            pp = 15,
            generationId = 1,
            shortEffect = "Effect $id",
        )
    }
}
