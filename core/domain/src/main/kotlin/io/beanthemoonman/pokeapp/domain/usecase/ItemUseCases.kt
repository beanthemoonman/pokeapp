package io.beanthemoonman.pokeapp.domain.usecase

import io.beanthemoonman.pokeapp.domain.model.Item
import io.beanthemoonman.pokeapp.domain.repository.ItemRepository
import javax.inject.Inject

/** Loads one page of the item dictionary (national, not generation-scoped). */
class GetItemPageUseCase @Inject constructor(
  private val repository: ItemRepository
) {
  suspend operator fun invoke(offset: Int, count: Int): List<Item> =
    repository.getItemPage(offset, count)

  companion object {
    const val PAGE_SIZE = 30
  }
}

/** Searches the item dictionary by name or id. */
class SearchItemsUseCase @Inject constructor(
  private val repository: ItemRepository
) {
  suspend operator fun invoke(query: String): List<Item> = repository.searchItems(query)
}

/** Loads full detail for a single item. */
class GetItemDetailUseCase @Inject constructor(
  private val repository: ItemRepository
) {
  suspend operator fun invoke(id: Int): Item = repository.getItemDetail(id)
}
