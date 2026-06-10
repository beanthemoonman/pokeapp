package io.beanthemoonman.pokeapp.phone.ui.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.phone.R
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.ui.common.theme.color
import io.beanthemoonman.pokeapp.uistate.team.TeamViewModel

/**
 * Team Builder route. Owns the [TeamViewModel], collects its [TeamViewModel.state] and
 * [TeamViewModel.picker] flows, and wires the stateless pieces (grid, coverage panel, picker
 * sheet) back to the ViewModel's intents.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
  modifier: Modifier = Modifier,
  viewModel: TeamViewModel = hiltViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val picker by viewModel.picker.collectAsStateWithLifecycle()

  val filledCount = (state as? UiState.Success)?.data?.filledCount ?: 0

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = PokedexColors.Background,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(R.string.team_title),
            color = PokedexColors.TextPrimary,
            fontWeight = FontWeight.Bold,
          )
        },
        actions = {
          Text(
            text = stringResource(R.string.team_count, filledCount),
            color = PokedexColors.TextDim,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(end = 16.dp),
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = PokedexColors.Background,
          titleContentColor = PokedexColors.TextPrimary,
        ),
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier
      .fillMaxSize()
      .padding(innerPadding)) {
      when (val s = state) {
        is UiState.Loading -> CircularProgressIndicator(
          color = Type.GRASS.color(),
          modifier = Modifier.align(Alignment.Center),
        )

        is UiState.Error -> Text(
          text = s.message,
          color = PokedexColors.TextDim,
          fontSize = 14.sp,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .align(Alignment.Center)
            .padding(horizontal = 40.dp),
        )

        is UiState.Success -> Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
          TeamSlotGrid(
            team = s.data.team,
            onSlotClick = viewModel::openPicker,
            onRemove = viewModel::removeSlot,
          )
          TeamCoveragePanel(
            coverage = s.data.coverage,
            hasMembers = s.data.hasMembers,
          )
        }
      }
    }
  }

  TeamPickerSheet(
    state = picker,
    onQueryChange = viewModel::onPickerQueryChange,
    onSelect = viewModel::selectPokemon,
    onRemove = viewModel::removeSlot,
    onDismiss = viewModel::closePicker,
  )
}
