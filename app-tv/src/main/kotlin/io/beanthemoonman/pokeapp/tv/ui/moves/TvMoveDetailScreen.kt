package io.beanthemoonman.pokeapp.tv.ui.moves

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.beanthemoonman.pokeapp.domain.model.Move
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.tv.R
import io.beanthemoonman.pokeapp.tv.ui.common.TvBackBar
import io.beanthemoonman.pokeapp.tv.ui.common.TvErrorState
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadge
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadgeSize
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.uistate.moves.MoveDetailViewModel

/** TV move detail — split stats panel + effect text (tv-screens.jsx `TVMoveDetail`). */
@Composable
fun TvMoveDetailScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: MoveDetailViewModel = hiltViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  Box(modifier = modifier
    .fillMaxSize()
    .background(PokedexColors.Background)) {
    when (val s = state) {
      is UiState.Loading -> Box(
        Modifier
          .fillMaxSize()
          .padding(44.dp)
      ) { TvBackBar(stringResource(R.string.moves_back)) }

      is UiState.Error -> TvErrorState(
        MovesAccent,
        stringResource(R.string.moves_error_title),
        stringResource(R.string.moves_error_body),
        viewModel::retry
      )

      is UiState.Success -> MoveDetailContent(s.data)
    }
  }
}

@Composable
private fun MoveDetailContent(move: Move) {
  Row(Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxWidth(0.46f)
        .fillMaxHeight()
        .border(1.dp, PokedexColors.Line)
        .padding(44.dp),
    ) {
      TvBackBar(stringResource(R.string.moves_back))
      Text(
        move.name,
        color = PokedexColors.TextPrimary,
        fontSize = 42.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.8).sp,
        modifier = Modifier.padding(bottom = 14.dp)
      )
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(bottom = 30.dp)
      ) {
        move.type?.let { TypeBadge(it, size = TypeBadgeSize.MD) }
        Text(
          stringResource(move.category.labelRes()).uppercase(),
          color = move.category.classColor(),
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MoveStat(
          stringResource(R.string.moves_stat_power),
          move.power?.toString() ?: stringResource(R.string.moves_value_none)
        )
        MoveStat(
          stringResource(R.string.moves_stat_acc),
          move.accuracy?.let { stringResource(R.string.moves_acc_value, it) }
            ?: stringResource(R.string.moves_value_none))
        MoveStat(stringResource(R.string.moves_stat_pp), move.pp.toString())
      }
    }
    Column(
      Modifier
        .weight(1f)
        .fillMaxHeight()
        .padding(44.dp)
        .verticalScroll(rememberScrollState())
    ) {
      Text(
        stringResource(R.string.moves_effect).uppercase(),
        color = PokedexColors.TextFaint,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.6.sp
      )
      Spacer(Modifier.height(10.dp))
      Text(
        move.shortEffect,
        color = PokedexColors.TextPrimary,
        fontSize = 16.sp,
        lineHeight = 26.sp
      )
    }
  }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.MoveStat(label: String, value: String) {
  Column(
    modifier = Modifier
      .weight(1f)
      .clip(RoundedCornerShape(14.dp))
      .background(PokedexColors.Surface)
      .border(1.dp, PokedexColors.Line, RoundedCornerShape(14.dp))
      .padding(vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      value,
      color = PokedexColors.TextPrimary,
      fontSize = 26.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
    Text(
      label.uppercase(),
      color = PokedexColors.TextFaint,
      fontSize = 10.5.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.2.sp,
      modifier = Modifier.padding(top = 6.dp)
    )
  }
}
