package io.beanthemoonman.pokeapp.tv.ui.nav

import io.beanthemoonman.pokeapp.tv.ui.common.TvNavItem

/** Top-level TV destinations. D-pad driven via the left nav rail — no bottom navigation. */
sealed class TvDestination(val route: String) {

    /** Root generation selector (first launch + "switch generation"). */
    data object VersionSelect : TvDestination("version_select")

    /** Browse grid (with the generation sidebar). */
    data object Browse : TvDestination("browse")

    /** Items dictionary. */
    data object Items : TvDestination("items")

    /** Moves dictionary. */
    data object Moves : TvDestination("moves")

    /** Team builder. */
    data object Team : TvDestination("team")

    /** Type matchup calculator. */
    data object Matchup : TvDestination("matchup")

    /** Pokémon detail, keyed by national dex id. */
    data object Detail : TvDestination("detail/{id}") {
        const val ARG_ID = "id"
        fun routeFor(id: Int) = "detail/$id"
    }

    /** Item detail, keyed by item id. */
    data object ItemDetail : TvDestination("item_detail/{id}") {
        const val ARG_ID = "id"
        fun routeFor(id: Int) = "item_detail/$id"
    }

    /** Move detail, keyed by move id. */
    data object MoveDetail : TvDestination("move_detail/{id}") {
        const val ARG_ID = "id"
        fun routeFor(id: Int) = "move_detail/$id"
    }

    companion object {
        /** Route for a top-level nav-rail destination. */
        fun routeFor(item: TvNavItem): String = when (item) {
            TvNavItem.DEX -> Browse.route
            TvNavItem.ITEMS -> Items.route
            TvNavItem.MOVES -> Moves.route
            TvNavItem.TEAM -> Team.route
            TvNavItem.MATCHUP -> Matchup.route
        }
    }
}
