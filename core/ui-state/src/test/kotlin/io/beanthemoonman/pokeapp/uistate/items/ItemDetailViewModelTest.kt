package io.beanthemoonman.pokeapp.uistate.items

import androidx.lifecycle.SavedStateHandle
import io.beanthemoonman.pokeapp.domain.model.Item
import io.beanthemoonman.pokeapp.domain.model.ItemCategory
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.repository.ItemRepository
import io.beanthemoonman.pokeapp.domain.usecase.GetItemDetailUseCase
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
class ItemDetailViewModelTest {

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
  fun `loads item into Success for the saved-state id`() = runTest(dispatcher) {
    val repo = FakeItemRepository()
    val vm = newViewModel(repo, id = 229)

    dispatcher.scheduler.advanceUntilIdle()

    val state = vm.state.value
    assertTrue(state is UiState.Success)
    assertEquals(229, (state as UiState.Success).data.id)
    assertEquals(229, repo.requestedId)
  }

  @Test
  fun `failure surfaces Error and retry recovers`() = runTest(dispatcher) {
    val repo = FakeItemRepository(throwing = true)
    val vm = newViewModel(repo, id = 229)

    dispatcher.scheduler.advanceUntilIdle()
    assertTrue(vm.state.value is UiState.Error)

    repo.throwing = false
    vm.retry()
    dispatcher.scheduler.advanceUntilIdle()
    assertTrue(vm.state.value is UiState.Success)
  }

  private fun newViewModel(repo: FakeItemRepository, id: Int) = ItemDetailViewModel(
    GetItemDetailUseCase(repo),
    SavedStateHandle(mapOf(ItemDetailViewModel.ARG_ID to id)),
  )

  private class FakeItemRepository(var throwing: Boolean = false) : ItemRepository {
    var requestedId: Int = -1

    override suspend fun getItemDetail(id: Int): Item {
      requestedId = id
      if (throwing) throw RuntimeException("boom")
      return Item(
        id = id,
        name = "Charizardite X",
        category = ItemCategory.MEGA_STONES,
        cost = 0,
        shortEffect = "Lets Charizard Mega Evolve.",
        flavor = "A Mega Stone.",
        spriteUrl = "https://example.com/item/$id.png",
      )
    }

    override suspend fun getItemPage(offset: Int, count: Int): List<Item> = emptyList()
    override suspend fun searchItems(query: String): List<Item> = emptyList()
  }
}
