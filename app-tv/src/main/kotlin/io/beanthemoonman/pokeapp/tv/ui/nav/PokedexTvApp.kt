package io.beanthemoonman.pokeapp.tv.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.beanthemoonman.pokeapp.tv.ui.AppStartViewModel
import io.beanthemoonman.pokeapp.tv.ui.StartState
import io.beanthemoonman.pokeapp.tv.ui.browse.BrowseScreen
import io.beanthemoonman.pokeapp.tv.ui.common.TvNavItem
import io.beanthemoonman.pokeapp.tv.ui.detail.TvDetailScreen
import io.beanthemoonman.pokeapp.tv.ui.items.TvItemDetailScreen
import io.beanthemoonman.pokeapp.tv.ui.items.TvItemsListScreen
import io.beanthemoonman.pokeapp.tv.ui.matchup.TvMatchupScreen
import io.beanthemoonman.pokeapp.tv.ui.moves.TvMoveDetailScreen
import io.beanthemoonman.pokeapp.tv.ui.moves.TvMovesListScreen
import io.beanthemoonman.pokeapp.tv.ui.team.TvTeamScreen
import io.beanthemoonman.pokeapp.tv.ui.version.TvVersionSelectScreen
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors

/** Entry point: gates between the root selector and the browse shell on launch. */
@Composable
fun PokedexTvApp(startViewModel: AppStartViewModel = hiltViewModel()) {
    val start by startViewModel.startState.collectAsStateWithLifecycle()
    when (start) {
        StartState.Loading -> Box(Modifier.fillMaxSize().background(PokedexColors.Background))
        StartState.Selector -> PokedexTvNavHost(startAtSelector = true)
        StartState.Shell -> PokedexTvNavHost(startAtSelector = false)
    }
}

@Composable
private fun PokedexTvNavHost(startAtSelector: Boolean) {
    val navController = rememberNavController()

    fun openShellAfterSelection() {
        navController.navigate(TvDestination.Browse.route) {
            popUpTo(TvDestination.VersionSelect.route) { inclusive = true }
            launchSingleTop = true
        }
    }

    val onNavigate: (TvNavItem) -> Unit = { item -> navController.switchTab(TvDestination.routeFor(item)) }
    val onSwitchGeneration: () -> Unit = { navController.navigate(TvDestination.VersionSelect.route) }

    NavHost(
        navController = navController,
        startDestination = if (startAtSelector) TvDestination.VersionSelect.route else TvDestination.Browse.route,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(TvDestination.VersionSelect.route) {
            TvVersionSelectScreen(onGenerationChosen = ::openShellAfterSelection)
        }
        composable(TvDestination.Browse.route) {
            BrowseScreen(
                onPokemonClick = { id -> navController.navigate(TvDestination.Detail.routeFor(id)) },
                onNavigate = onNavigate,
                onSwitchGeneration = onSwitchGeneration,
            )
        }
        composable(TvDestination.Items.route) {
            TvItemsListScreen(
                onItemClick = { id -> navController.navigate(TvDestination.ItemDetail.routeFor(id)) },
                onNavigate = onNavigate,
                onSwitchGeneration = onSwitchGeneration,
            )
        }
        composable(TvDestination.Moves.route) {
            TvMovesListScreen(
                onMoveClick = { id -> navController.navigate(TvDestination.MoveDetail.routeFor(id)) },
                onNavigate = onNavigate,
                onSwitchGeneration = onSwitchGeneration,
            )
        }
        composable(TvDestination.Team.route) {
            TvTeamScreen(onNavigate = onNavigate)
        }
        composable(TvDestination.Matchup.route) {
            TvMatchupScreen(onNavigate = onNavigate, onSwitchGeneration = onSwitchGeneration)
        }
        composable(
            route = TvDestination.Detail.route,
            arguments = listOf(navArgument(TvDestination.Detail.ARG_ID) { type = NavType.IntType }),
        ) {
            TvDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = TvDestination.ItemDetail.route,
            arguments = listOf(navArgument(TvDestination.ItemDetail.ARG_ID) { type = NavType.IntType }),
        ) {
            TvItemDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = TvDestination.MoveDetail.route,
            arguments = listOf(navArgument(TvDestination.MoveDetail.ARG_ID) { type = NavType.IntType }),
        ) {
            TvMoveDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}

/** Switch between top-level rail destinations, preserving each tab's back stack + state. */
private fun NavController.switchTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
