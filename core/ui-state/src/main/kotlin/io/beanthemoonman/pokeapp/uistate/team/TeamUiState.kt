package io.beanthemoonman.pokeapp.uistate.team

import io.beanthemoonman.pokeapp.domain.model.Generation
import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.model.TeamCoverage

/**
 * Payload for the Team Builder's [io.beanthemoonman.pokeapp.domain.model.UiState.Success] state.
 * [team] is always six ordered slots (a null entry is empty), and [coverage] is the static type
 * analysis of whichever members are filled.
 */
data class TeamData(
    val generation: Generation,
    val team: List<Pokemon?>,
    val coverage: TeamCoverage,
) {
    val filledCount: Int get() = team.count { it != null }
    val hasMembers: Boolean get() = filledCount > 0
}

/**
 * The "add / replace a member" picker overlay. [Closed] hides it; [Open] targets a specific team
 * [slot] and carries the live search [query] + [results]. [replacing] flags that the slot is
 * already filled, so the overlay can offer a Remove action.
 */
sealed interface TeamPickerUiState {
    data object Closed : TeamPickerUiState
    data class Open(
        val slot: Int,
        val replacing: Boolean,
        val query: String,
        val results: PickerResults,
    ) : TeamPickerUiState
}

/** Result state for the picker's search box. Idle = blank query (prompt). */
sealed interface PickerResults {
    data object Idle : PickerResults
    data object Loading : PickerResults
    data object Empty : PickerResults
    data class Results(val items: List<Pokemon>) : PickerResults
    data object Error : PickerResults
}
