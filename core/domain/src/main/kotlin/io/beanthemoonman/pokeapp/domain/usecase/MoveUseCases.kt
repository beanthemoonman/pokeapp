package io.beanthemoonman.pokeapp.domain.usecase

import io.beanthemoonman.pokeapp.domain.model.Move
import io.beanthemoonman.pokeapp.domain.repository.MoveRepository
import javax.inject.Inject

/** Loads one page of the move dictionary, scoped to a generation. */
class GetMovePageUseCase @Inject constructor(
    private val repository: MoveRepository
) {
    suspend operator fun invoke(generationId: Int, offset: Int, count: Int): List<Move> =
        repository.getMovePage(generationId, offset, count)

    companion object {
        const val PAGE_SIZE = 30
    }
}

/** Searches the move dictionary (scoped to a generation) by name or id. */
class SearchMovesUseCase @Inject constructor(
    private val repository: MoveRepository
) {
    suspend operator fun invoke(generationId: Int, query: String): List<Move> =
        repository.searchMoves(generationId, query)
}

/** Loads full detail for a single move. */
class GetMoveDetailUseCase @Inject constructor(
    private val repository: MoveRepository
) {
    suspend operator fun invoke(id: Int): Move = repository.getMoveDetail(id)
}
