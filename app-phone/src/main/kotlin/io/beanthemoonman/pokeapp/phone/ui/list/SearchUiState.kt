package io.beanthemoonman.pokeapp.phone.ui.list

import io.beanthemoonman.pokeapp.domain.model.Pokemon

/**
 * State of the list-screen search overlay. Distinct from the paged-list [UiState] so the
 * dex keeps its scroll position underneath while a query is active.
 *
 * - [Idle]    — no active query; the paged list is shown instead.
 * - [Loading] — query accepted, results in flight (renders skeleton rows).
 * - [Results] — at least one match.
 * - [Empty]   — query ran but matched nothing.
 * - [Error]   — the search failed (e.g. offline name-index fetch).
 */
sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Results(val items: List<Pokemon>) : SearchUiState
    data object Empty : SearchUiState
    data class Error(val message: String) : SearchUiState
}
