package io.beanthemoonman.pokeapp.phone.ui.nav

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CatchingPokemon
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.ui.graphics.vector.ImageVector
import io.beanthemoonman.pokeapp.phone.R

/** Top-level bottom-navigation destinations. */
sealed class NavDestination(val route: String) {

    /** Tabs that appear in the bottom navigation bar. */
    sealed class Tab(
        route: String,
        @param:StringRes val label: Int,
        val icon: ImageVector,
    ) : NavDestination(route) {
        data object List : Tab("list", R.string.nav_dex, Icons.Outlined.CatchingPokemon)
        data object Team : Tab("team", R.string.nav_team, Icons.Outlined.Groups)
        data object TypeCalc : Tab("typecalc", R.string.nav_calc, Icons.Outlined.Shield)

        companion object {
            val all = listOf(List, Team, TypeCalc)
        }
    }

    /** Pokémon detail, keyed by national dex id. */
    data object Detail : NavDestination("detail/{id}") {
        const val ARG_ID = "id"
        fun routeFor(id: Int) = "detail/$id"
    }
}
