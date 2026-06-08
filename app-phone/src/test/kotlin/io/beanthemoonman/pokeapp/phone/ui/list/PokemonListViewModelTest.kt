package io.beanthemoonman.pokeapp.phone.ui.list

import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.model.Stats
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.repository.GenerationRepository
import io.beanthemoonman.pokeapp.domain.repository.PokemonRepository
import io.beanthemoonman.pokeapp.domain.usecase.GetPokemonPageUseCase
import io.beanthemoonman.pokeapp.domain.usecase.ObserveSelectedGenerationUseCase
import io.beanthemoonman.pokeapp.domain.usecase.SearchPokemonUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
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
import kotlin.time.Duration.Companion.milliseconds

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
    fun `first page loads into Success`() = runTest(dispatcher) {
        val vm = newViewModel(FakeRepository(total = 151))

        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is UiState.Success)
        val data = (state as UiState.Success).data
        assertEquals(GetPokemonPageUseCase.PAGE_SIZE, data.items.size)
        assertEquals(1, data.items.first().id)
        assertFalse(data.endReached)
    }

    @Test
    fun `first page failure surfaces Error`() = runTest(dispatcher) {
        val vm = newViewModel(FakeRepository(total = 151, throwing = true))

        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state.value is UiState.Error)
    }

    @Test
    fun `loadMore appends the next page`() = runTest(dispatcher) {
        val vm = newViewModel(FakeRepository(total = 151))
        dispatcher.scheduler.advanceUntilIdle()

        vm.loadMore()
        dispatcher.scheduler.advanceUntilIdle()

        val data = (vm.state.value as UiState.Success).data
        assertEquals(GetPokemonPageUseCase.PAGE_SIZE * 2, data.items.size)
        // Pages are contiguous National Dex ids.
        assertEquals((1..data.items.size).toList(), data.items.map { it.id })
    }

    @Test
    fun `paging stops at the end of the dex`() = runTest(dispatcher) {
        // Only 40 entries available: page 1 = 30, page 2 = 10 (< PAGE_SIZE) → end.
        val vm = newViewModel(FakeRepository(total = 40))
        dispatcher.scheduler.advanceUntilIdle()

        vm.loadMore()
        dispatcher.scheduler.advanceUntilIdle()

        val data = (vm.state.value as UiState.Success).data
        assertEquals(40, data.items.size)
        assertTrue(data.endReached)
    }

    @Test
    fun `switching generation resets paging and keeps paging`() = runTest(dispatcher) {
        val genRepo = FakeGenerationRepository(selectedId = 1) // Gen I, dexEnd 151
        val repo = FakeRepository(total = 400)
        val vm = PokemonListViewModel(
            GetPokemonPageUseCase(repo),
            SearchPokemonUseCase(repo),
            ObserveSelectedGenerationUseCase(genRepo),
        )
        dispatcher.scheduler.advanceUntilIdle()
        vm.loadMore()
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(GetPokemonPageUseCase.PAGE_SIZE * 2, (vm.state.value as UiState.Success).data.items.size)

        // Switch to Gen III — paging must restart from page 1, not stay where it was.
        genRepo.selectGeneration(3)
        dispatcher.scheduler.advanceUntilIdle()
        val afterSwitch = (vm.state.value as UiState.Success).data
        assertEquals(GetPokemonPageUseCase.PAGE_SIZE, afterSwitch.items.size)
        assertEquals(1, afterSwitch.items.first().id)

        // ...and loadMore still appends after the switch.
        vm.loadMore()
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(GetPokemonPageUseCase.PAGE_SIZE * 2, (vm.state.value as UiState.Success).data.items.size)
    }

    @Test
    fun `switching while a page load is in flight does not corrupt paging`() = runTest(dispatcher) {
        val genRepo = FakeGenerationRepository(selectedId = 1)
        val repo = FakeRepository(total = 400, delayMillis = 100)
        val vm = PokemonListViewModel(
            GetPokemonPageUseCase(repo),
            SearchPokemonUseCase(repo),
            ObserveSelectedGenerationUseCase(genRepo),
        )
        dispatcher.scheduler.advanceUntilIdle() // first page of Gen I

        // Begin an append, then switch generation while it is still in flight.
        vm.loadMore()
        dispatcher.scheduler.runCurrent() // let loadMore start + suspend (isAppending = true)
        genRepo.selectGeneration(3)        // cancels the in-flight append
        dispatcher.scheduler.advanceUntilIdle()

        val data = (vm.state.value as UiState.Success).data
        // The cancelled append must NOT have flipped appendError / left isAppending stuck.
        assertFalse(data.appendError)
        assertFalse(data.isAppending)
        assertEquals(GetPokemonPageUseCase.PAGE_SIZE, data.items.size)
        assertEquals(1, data.items.first().id)

        // Paging still works after the switch.
        vm.loadMore()
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(GetPokemonPageUseCase.PAGE_SIZE * 2, (vm.state.value as UiState.Success).data.items.size)
    }

    @Test
    fun `blank query keeps search Idle`() = runTest(dispatcher) {
        val vm = newViewModel(FakeRepository(total = 151))
        backgroundScope.launch { vm.searchState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.searchState.value is SearchUiState.Idle)
    }

    @Test
    fun `name query within the dex yields Results`() = runTest(dispatcher) {
        val vm = newViewModel(FakeRepository(total = 151))
        backgroundScope.launch { vm.searchState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        vm.onQueryChange("Pokemon1")
        dispatcher.scheduler.advanceUntilIdle()

        val s = vm.searchState.value
        assertTrue(s is SearchUiState.Results)
        // Every match's name contains the query and is inside Gen I (id <= 151).
        assertTrue((s as SearchUiState.Results).items.all { it.name.contains("Pokemon1") && it.id <= 151 })
    }

    @Test
    fun `dex number outside the generation yields Empty`() = runTest(dispatcher) {
        // Gen I dexEnd = 151; #200 is out of range even though the repo has 400 entries.
        val genRepo = FakeGenerationRepository(selectedId = 1)
        val repo = FakeRepository(total = 400)
        val vm = PokemonListViewModel(
            GetPokemonPageUseCase(repo),
            SearchPokemonUseCase(repo),
            ObserveSelectedGenerationUseCase(genRepo),
        )
        backgroundScope.launch { vm.searchState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        vm.onQueryChange("200")
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.searchState.value is SearchUiState.Empty)
    }

    @Test
    fun `clearing the query returns to Idle`() = runTest(dispatcher) {
        val vm = newViewModel(FakeRepository(total = 151))
        backgroundScope.launch { vm.searchState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        vm.onQueryChange("Pokemon1")
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.searchState.value is SearchUiState.Results)

        vm.clearSearch()
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.searchState.value is SearchUiState.Idle)
    }

    @Test
    fun `search failure surfaces Error and retry recovers`() = runTest(dispatcher) {
        val genRepo = FakeGenerationRepository(selectedId = 1)
        val repo = FakeRepository(total = 151, searchThrowing = true)
        val vm = PokemonListViewModel(
            GetPokemonPageUseCase(repo),
            SearchPokemonUseCase(repo),
            ObserveSelectedGenerationUseCase(genRepo),
        )
        backgroundScope.launch { vm.searchState.collect {} }
        dispatcher.scheduler.advanceUntilIdle()

        vm.onQueryChange("Pokemon1")
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.searchState.value is SearchUiState.Error)

        // Stop the repo throwing, then retry the same query — it must re-run.
        repo.failSearch = false
        vm.retrySearch()
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.searchState.value is SearchUiState.Results)
    }

    private fun newViewModel(repo: FakeRepository) = PokemonListViewModel(
        GetPokemonPageUseCase(repo),
        SearchPokemonUseCase(repo),
        ObserveSelectedGenerationUseCase(FakeGenerationRepository(selectedId = 1)),
    )

    private class FakeRepository(
        private val total: Int,
        private val throwing: Boolean = false,
        private val delayMillis: Long = 0,
        searchThrowing: Boolean = false,
    ) : PokemonRepository {
        /** Toggleable so a test can fail a search then recover on retry. */
        var failSearch: Boolean = searchThrowing
        override suspend fun getPokemonPage(startId: Int, count: Int): List<Pokemon> {
            if (delayMillis > 0) delay(delayMillis.milliseconds)
            if (throwing) throw RuntimeException("boom")
            val end = minOf(startId + count - 1, total)
            if (startId > end) return emptyList()
            return (startId..end).map { pokemon(it) }
        }

        override suspend fun searchPokemon(query: String, dexEnd: Int): List<Pokemon> {
            if (delayMillis > 0) delay(delayMillis.milliseconds)
            if (failSearch) throw RuntimeException("search boom")
            val q = query.trim()
            if (q.isEmpty()) return emptyList()
            val limit = minOf(dexEnd, total)
            q.toIntOrNull()?.let { number ->
                return if (number in 1..limit) listOf(pokemon(number)) else emptyList()
            }
            return (1..limit).map { pokemon(it) }.filter { it.name.contains(q, ignoreCase = true) }
        }

        override suspend fun getPokemonDetail(id: Int): Pokemon = pokemon(id)
        override fun getTeam(): Flow<List<Pokemon?>> = flow { emit(emptyList()) }
        override suspend fun setTeamSlot(slot: Int, pokemonId: Int?) = Unit
    }

    private class FakeGenerationRepository(selectedId: Int?) : GenerationRepository {
        private val flow = MutableStateFlow(selectedId)
        override val selectedGenerationId: Flow<Int?> = flow
        override suspend fun selectGeneration(id: Int) {
            flow.value = id
        }
    }

    private companion object {
        fun pokemon(id: Int) = Pokemon(
            id = id,
            name = "Pokemon$id",
            spriteUrl = "https://example.com/$id.png",
            types = listOf(Type.NORMAL),
            stats = Stats(1, 1, 1, 1, 1, 1),
            height = 1,
            weight = 1,
        )
    }
}
