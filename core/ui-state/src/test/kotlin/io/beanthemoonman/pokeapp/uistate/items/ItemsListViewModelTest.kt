package io.beanthemoonman.pokeapp.uistate.items

import io.beanthemoonman.pokeapp.domain.model.Item
import io.beanthemoonman.pokeapp.domain.model.ItemCategory
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.repository.GenerationRepository
import io.beanthemoonman.pokeapp.domain.repository.ItemRepository
import io.beanthemoonman.pokeapp.domain.usecase.GetItemPageUseCase
import io.beanthemoonman.pokeapp.domain.usecase.ObserveSelectedGenerationUseCase
import io.beanthemoonman.pokeapp.domain.usecase.SearchItemsUseCase
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
class ItemsListViewModelTest {

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
    val vm = newViewModel(FakeItemRepository(total = 100))
    dispatcher.scheduler.advanceUntilIdle()

    val data = (vm.state.value as UiState.Success).data
    assertEquals(GetItemPageUseCase.PAGE_SIZE, data.items.size)
    assertEquals(1, data.items.first().id)
    assertFalse(data.endReached)
  }

  @Test
  fun `first page failure surfaces Error`() = runTest(dispatcher) {
    val vm = newViewModel(FakeItemRepository(total = 100, throwing = true))
    dispatcher.scheduler.advanceUntilIdle()

    assertTrue(vm.state.value is UiState.Error)
  }

  @Test
  fun `loadMore appends the next page`() = runTest(dispatcher) {
    val vm = newViewModel(FakeItemRepository(total = 100))
    dispatcher.scheduler.advanceUntilIdle()

    vm.loadMore()
    dispatcher.scheduler.advanceUntilIdle()

    val data = (vm.state.value as UiState.Success).data
    assertEquals(GetItemPageUseCase.PAGE_SIZE * 2, data.items.size)
  }

  @Test
  fun `paging stops at the end of the dictionary`() = runTest(dispatcher) {
    // 40 total: page 1 = 30, page 2 = 10 (< PAGE_SIZE) → end.
    val vm = newViewModel(FakeItemRepository(total = 40))
    dispatcher.scheduler.advanceUntilIdle()

    vm.loadMore()
    dispatcher.scheduler.advanceUntilIdle()

    val data = (vm.state.value as UiState.Success).data
    assertEquals(40, data.items.size)
    assertTrue(data.endReached)
  }

  @Test
  fun `selecting a category filters the visible list`() = runTest(dispatcher) {
    val vm = newViewModel(FakeItemRepository(total = 30))
    dispatcher.scheduler.advanceUntilIdle()

    vm.selectCategory(ItemCategory.HEALING)
    val data = (vm.state.value as UiState.Success).data
    assertEquals(ItemCategory.HEALING, data.category)
    assertTrue(data.visible.all { it.category == ItemCategory.HEALING })
    // All items are still loaded; only the displayed subset changes.
    assertEquals(30, data.items.size)

    vm.selectCategory(null)
    assertEquals(30, (vm.state.value as UiState.Success).data.visible.size)
  }

  @Test
  fun `blank query keeps search Idle`() = runTest(dispatcher) {
    val vm = newViewModel(FakeItemRepository(total = 30))
    backgroundScope.launch { vm.searchState.collect {} }
    dispatcher.scheduler.advanceUntilIdle()

    assertTrue(vm.searchState.value is ItemsSearchUiState.Idle)
  }

  @Test
  fun `name query yields Results`() = runTest(dispatcher) {
    val vm = newViewModel(FakeItemRepository(total = 30))
    backgroundScope.launch { vm.searchState.collect {} }
    dispatcher.scheduler.advanceUntilIdle()

    vm.onQueryChange("Item1")
    dispatcher.scheduler.advanceUntilIdle()

    val s = vm.searchState.value
    assertTrue(s is ItemsSearchUiState.Results)
    assertTrue((s as ItemsSearchUiState.Results).items.all { it.name.contains("Item1") })
  }

  @Test
  fun `unmatched query yields Empty`() = runTest(dispatcher) {
    val vm = newViewModel(FakeItemRepository(total = 30))
    backgroundScope.launch { vm.searchState.collect {} }
    dispatcher.scheduler.advanceUntilIdle()

    vm.onQueryChange("zzz-nope")
    dispatcher.scheduler.advanceUntilIdle()

    assertTrue(vm.searchState.value is ItemsSearchUiState.Empty)
  }

  @Test
  fun `search failure surfaces Error and retry recovers`() = runTest(dispatcher) {
    val repo = FakeItemRepository(total = 30, searchThrowing = true)
    val vm = newViewModel(repo)
    backgroundScope.launch { vm.searchState.collect {} }
    dispatcher.scheduler.advanceUntilIdle()

    vm.onQueryChange("Item1")
    dispatcher.scheduler.advanceUntilIdle()
    assertTrue(vm.searchState.value is ItemsSearchUiState.Error)

    repo.failSearch = false
    vm.retrySearch()
    dispatcher.scheduler.advanceUntilIdle()
    assertTrue(vm.searchState.value is ItemsSearchUiState.Results)
  }

  private fun newViewModel(repo: FakeItemRepository) = ItemsListViewModel(
    GetItemPageUseCase(repo),
    SearchItemsUseCase(repo),
    ObserveSelectedGenerationUseCase(FakeGenerationRepository(selectedId = 1)),
  )

  private class FakeItemRepository(
    private val total: Int,
    private val throwing: Boolean = false,
    searchThrowing: Boolean = false,
  ) : ItemRepository {
    var failSearch: Boolean = searchThrowing

    override suspend fun getItemPage(offset: Int, count: Int): List<Item> {
      if (throwing) throw RuntimeException("boom")
      val startId = offset + 1
      val end = minOf(offset + count, total)
      if (startId > end) return emptyList()
      return (startId..end).map { item(it) }
    }

    override suspend fun searchItems(query: String): List<Item> {
      if (failSearch) throw RuntimeException("search boom")
      val q = query.trim()
      if (q.isEmpty()) return emptyList()
      q.toIntOrNull()?.let { id ->
        return if (id in 1..total) listOf(item(id)) else emptyList()
      }
      return (1..total).map { item(it) }.filter { it.name.contains(q, ignoreCase = true) }
    }

    override suspend fun getItemDetail(id: Int): Item = item(id)
  }

  private class FakeGenerationRepository(selectedId: Int?) : GenerationRepository {
    private val flow = MutableStateFlow(selectedId)
    override val selectedGenerationId: Flow<Int?> = flow
    override suspend fun selectGeneration(id: Int) {
      flow.value = id
    }
  }

  private companion object {
    /** Alternates categories so category-filter tests have a non-trivial split. */
    fun item(id: Int) = Item(
      id = id,
      name = "Item$id",
      category = if (id % 2 == 0) ItemCategory.HEALING else ItemCategory.POKE_BALLS,
      cost = id * 10,
      shortEffect = "Effect $id",
      flavor = "Flavor $id",
      spriteUrl = "https://example.com/item/$id.png",
    )
  }
}
