package io.beanthemoonman.pokeapp.uistate.moves

import io.beanthemoonman.pokeapp.domain.model.Move

/** Search box state for the moves list, independent of the paged dictionary. */
sealed interface MovesSearchUiState {
  /** Blank query — the paged dictionary is shown instead of results. */
  data object Idle : MovesSearchUiState
  data object Loading : MovesSearchUiState
  data object Empty : MovesSearchUiState
  data class Error(val message: String) : MovesSearchUiState
  data class Results(val moves: List<Move>) : MovesSearchUiState
}
