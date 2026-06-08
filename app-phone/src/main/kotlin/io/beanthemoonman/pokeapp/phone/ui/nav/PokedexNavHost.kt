package io.beanthemoonman.pokeapp.phone.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.beanthemoonman.pokeapp.phone.R
import io.beanthemoonman.pokeapp.phone.ui.AppStartViewModel
import io.beanthemoonman.pokeapp.phone.ui.StartState
import io.beanthemoonman.pokeapp.phone.ui.detail.PokemonDetailScreen
import io.beanthemoonman.pokeapp.phone.ui.items.ItemDetailScreen
import io.beanthemoonman.pokeapp.phone.ui.items.ItemsListScreen
import io.beanthemoonman.pokeapp.phone.ui.list.PokemonListScreen
import io.beanthemoonman.pokeapp.phone.ui.moves.MoveDetailScreen
import io.beanthemoonman.pokeapp.phone.ui.moves.MovesListScreen
import io.beanthemoonman.pokeapp.phone.ui.team.TeamScreen
import io.beanthemoonman.pokeapp.phone.ui.typecalc.TypeMatchupScreen
import io.beanthemoonman.pokeapp.phone.ui.version.VersionSelectScreen
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.ui.common.theme.color
import io.beanthemoonman.pokeapp.domain.model.Type

/** Entry point: gates between the root selector and the main shell on launch. */
@Composable
fun PokedexApp(startViewModel: AppStartViewModel = hiltViewModel()) {
    val start by startViewModel.startState.collectAsStateWithLifecycle()
    when (start) {
        StartState.Loading -> Box(Modifier.fillMaxSize().background(PokedexColors.Background))
        StartState.Selector -> PokedexNavHost(startAtSelector = true)
        StartState.Shell -> PokedexNavHost(startAtSelector = false)
    }
}

@Composable
fun PokedexNavHost(startAtSelector: Boolean) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    fun openShellAfterSelection() {
        navController.navigate(NavDestination.Tab.List.route) {
            popUpTo(NavDestination.VersionSelect.route) { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        bottomBar = {
            // Bar shows only on the tab destinations, never on selector/detail.
            if (currentRoute in NavDestination.Tab.all.map { it.route }) {
                BottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { tab ->
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (startAtSelector) {
                NavDestination.VersionSelect.route
            } else {
                NavDestination.Tab.List.route
            },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            composable(NavDestination.VersionSelect.route) {
                VersionSelectScreen(onGenerationChosen = ::openShellAfterSelection)
            }
            composable(NavDestination.Tab.List.route) {
                PokemonListScreen(
                    onPokemonClick = { id -> navController.navigate(NavDestination.Detail.routeFor(id)) },
                    onSwitchGeneration = { navController.navigate(NavDestination.VersionSelect.route) },
                )
            }
            composable(NavDestination.Tab.Items.route) {
                ItemsListScreen(
                    onItemClick = { id -> navController.navigate(NavDestination.ItemDetail.routeFor(id)) },
                    onSwitchGeneration = { navController.navigate(NavDestination.VersionSelect.route) },
                )
            }
            composable(NavDestination.Tab.Moves.route) {
                MovesListScreen(
                    onMoveClick = { id -> navController.navigate(NavDestination.MoveDetail.routeFor(id)) },
                    onSwitchGeneration = { navController.navigate(NavDestination.VersionSelect.route) },
                )
            }
            composable(NavDestination.Tab.Team.route) {
                TeamScreen()
            }
            composable(NavDestination.Tab.TypeCalc.route) {
                TypeMatchupScreen()
            }
            composable(
                route = NavDestination.Detail.route,
                arguments = listOf(navArgument(NavDestination.Detail.ARG_ID) { type = NavType.IntType }),
            ) {
                PokemonDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = NavDestination.ItemDetail.route,
                arguments = listOf(navArgument(NavDestination.ItemDetail.ARG_ID) { type = NavType.IntType }),
            ) {
                ItemDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = NavDestination.MoveDetail.route,
                arguments = listOf(navArgument(NavDestination.MoveDetail.ARG_ID) { type = NavType.IntType }),
            ) {
                MoveDetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun BottomBar(
    currentRoute: String?,
    onTabSelected: (NavDestination.Tab) -> Unit,
) {
    NavigationBar(containerColor = PokedexColors.Surface) {
        NavDestination.Tab.all.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onTabSelected(tab) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.label)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Type.FIRE.color(),
                    selectedTextColor = Type.FIRE.color(),
                    indicatorColor = PokedexColors.SurfaceRaised,
                    unselectedIconColor = PokedexColors.TextFaint,
                    unselectedTextColor = PokedexColors.TextFaint,
                ),
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(labelRes: Int, subtitle: String? = null) {
    Box(modifier = Modifier.fillMaxSize().background(PokedexColors.Background), contentAlignment = Alignment.Center) {
        Text(
            text = subtitle ?: (stringResource(labelRes) + " · " + stringResource(R.string.coming_soon)),
            color = PokedexColors.TextDim,
        )
    }
}
