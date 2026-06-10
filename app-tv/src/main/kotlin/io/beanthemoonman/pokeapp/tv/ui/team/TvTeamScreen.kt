package io.beanthemoonman.pokeapp.tv.ui.team

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.model.TeamCoverage
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.tv.R
import io.beanthemoonman.pokeapp.tv.ui.common.TvNavItem
import io.beanthemoonman.pokeapp.tv.ui.common.TvScreenScaffold
import io.beanthemoonman.pokeapp.tv.ui.common.TvSearchField
import io.beanthemoonman.pokeapp.tv.ui.common.tvFocusRing
import io.beanthemoonman.pokeapp.ui.common.component.PokemonSprite
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadge
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadgeSize
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.ui.common.theme.color
import io.beanthemoonman.pokeapp.ui.common.theme.onColor
import io.beanthemoonman.pokeapp.uistate.team.PickerResults
import io.beanthemoonman.pokeapp.uistate.team.TeamData
import io.beanthemoonman.pokeapp.uistate.team.TeamPickerUiState
import io.beanthemoonman.pokeapp.uistate.team.TeamViewModel
import androidx.compose.foundation.lazy.grid.items as gridItems

private val TeamAccent = Type.GRASS.color()
private val WeakOutline = Color(0xFFFF6B5C)

/** TV Team Builder — slots row + defensive/offensive coverage (tv-screens.jsx `TVTeam`). */
@Composable
fun TvTeamScreen(
  onNavigate: (TvNavItem) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: TeamViewModel = hiltViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val picker by viewModel.picker.collectAsStateWithLifecycle()

  Box(Modifier.fillMaxSize()) {
    TvScreenScaffold(active = TvNavItem.TEAM, onNavigate = onNavigate, modifier = modifier) {
      when (val s = state) {
        is UiState.Success -> TeamContent(s.data, onSlotClick = viewModel::openPicker)
        else -> Box(Modifier.fillMaxSize())
      }
    }
    (picker as? TeamPickerUiState.Open)?.let { open ->
      PickerOverlay(
        open = open,
        onQueryChange = viewModel::onPickerQueryChange,
        onSelect = viewModel::selectPokemon,
        onRemove = { viewModel.removeSlot(open.slot) },
        onClose = viewModel::closePicker,
      )
    }
  }
}

@Composable
private fun TeamContent(data: TeamData, onSlotClick: (Int) -> Unit) {
  Column(Modifier.fillMaxSize()) {
    // Header
    Row(
      Modifier
        .fillMaxWidth()
        .padding(bottom = 22.dp),
      verticalAlignment = Alignment.Bottom,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(
          stringResource(R.string.team_title),
          color = PokedexColors.TextPrimary,
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = (-0.6).sp
        )
        Text(
          stringResource(R.string.team_count, data.filledCount),
          color = PokedexColors.TextFaint,
          fontSize = 12.5.sp,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.padding(top = 4.dp)
        )
      }
      if (data.coverage.weakPointCount > 0) {
        Text(
          stringResource(R.string.team_weaknesses, data.coverage.weakPointCount),
          color = Color(0xFFE08A4A),
          fontSize = 13.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }
    // Slots row
    Row(
      Modifier
        .fillMaxWidth()
        .height(168.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      for (index in data.team.indices) {
        TeamSlot(
          pokemon = data.team[index],
          onClick = { onSlotClick(index) },
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
        )
      }
    }
    Spacer(Modifier.height(28.dp))
    // Coverage panels
    if (!data.hasMembers) {
      Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
          stringResource(R.string.team_coverage_empty),
          color = PokedexColors.TextDim,
          fontSize = 14.sp
        )
      }
    } else {
      Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        DefensivePanel(data.coverage, Modifier
          .weight(1.4f)
          .fillMaxHeight())
        OffensivePanel(data.coverage, Modifier
          .weight(1f)
          .fillMaxHeight())
      }
    }
  }
}

@Composable
private fun TeamSlot(pokemon: Pokemon?, onClick: () -> Unit, modifier: Modifier = Modifier) {
  val interaction = remember { MutableInteractionSource() }
  val focused by interaction.collectIsFocusedAsState()
  val accent = pokemon?.types?.first()?.color() ?: TeamAccent
  val base = modifier
    .clip(RoundedCornerShape(16.dp))
    .then(if (pokemon != null) Modifier.background(accent.copy(alpha = 0.16f)) else Modifier)
    .border(
      if (pokemon != null) 1.dp else 1.5.dp,
      if (pokemon != null) accent.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.18f),
      RoundedCornerShape(16.dp)
    )
    .tvFocusRing(focused = focused, accent = accent, cornerRadius = 16.dp)
    .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    .focusable(interactionSource = interaction)
    .padding(16.dp)
  if (pokemon == null) {
    Column(
      base,
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        Icons.Outlined.Add,
        contentDescription = null,
        tint = PokedexColors.TextFaint,
        modifier = Modifier.size(26.dp)
      )
      Text(
        stringResource(R.string.team_slot_empty),
        color = PokedexColors.TextFaint,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 10.dp)
      )
    }
  } else {
    Column(
      base,
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      PokemonSprite(
        spriteUrl = pokemon.spriteUrl,
        contentDescription = stringResource(R.string.sprite_cd, pokemon.name),
        modifier = Modifier.size(62.dp)
      )
      Text(
        pokemon.name,
        color = PokedexColors.TextPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp)
      )
      Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.padding(top = 6.dp)
      ) {
        pokemon.types.forEach { TypeBadge(it, size = TypeBadgeSize.SM) }
      }
    }
  }
}

@Composable
private fun DefensivePanel(coverage: TeamCoverage, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(PokedexColors.Surface)
      .border(1.dp, PokedexColors.Line, RoundedCornerShape(16.dp))
      .padding(horizontal = 26.dp, vertical = 22.dp),
  ) {
    SectionLabel(stringResource(R.string.team_coverage_defensive, coverage.roster.size))
    Spacer(Modifier.height(18.dp))
    LazyVerticalGrid(
      columns = GridCells.Fixed(9),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.weight(1f, fill = false)
    ) {
      gridItems(items = coverage.roster, key = { it.name }) { type ->
        val weak = coverage.defensiveWeaknesses[type] ?: 0
        CoverageCell(type, weak)
      }
    }
    Spacer(Modifier.height(20.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
      LegendDot(WeakOutline, stringResource(R.string.team_coverage_legend_shared))
      LegendDot(
        Color.White.copy(alpha = 0.16f),
        stringResource(R.string.team_coverage_legend_covered)
      )
    }
  }
}

@Composable
private fun CoverageCell(type: Type, weak: Int) {
  val c = type.color()
  Box(
    modifier = Modifier
      .aspectRatio(1f)
      .clip(RoundedCornerShape(9.dp))
      .background(if (weak > 0) c else c.copy(alpha = 0.14f))
      .then(
        if (weak > 0) Modifier.border(
          2.dp,
          WeakOutline,
          RoundedCornerShape(9.dp)
        ) else Modifier
      ),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = type.name.take(3),
      color = if (weak > 0) type.onColor() else c.copy(alpha = 0.9f),
      fontSize = 9.5.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
    )
    if (weak > 1) {
      Box(
        Modifier
          .align(Alignment.TopEnd)
          .size(16.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(WeakOutline), contentAlignment = Alignment.Center
      ) {
        Text(
          weak.toString(),
          color = Color(0xFF1A0F0D),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}

@Composable
private fun OffensivePanel(coverage: TeamCoverage, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(PokedexColors.Surface)
      .border(1.dp, PokedexColors.Line, RoundedCornerShape(16.dp))
      .padding(horizontal = 26.dp, vertical = 22.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    SectionLabel(stringResource(R.string.team_offensive_gaps))
    if (coverage.offensiveGaps.isEmpty()) {
      Text(
        stringResource(R.string.team_offensive_none),
        color = PokedexColors.TextDim,
        fontSize = 13.sp,
        lineHeight = 21.sp
      )
    } else {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        coverage.offensiveGaps.take(8)
          .forEach { TypeBadge(it, size = TypeBadgeSize.MD, soft = true) }
      }
      Text(
        stringResource(R.string.team_offensive_body),
        color = PokedexColors.TextDim,
        fontSize = 13.sp,
        lineHeight = 21.sp
      )
    }
  }
}

@Composable
private fun LegendDot(color: Color, label: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Box(Modifier
      .size(12.dp)
      .clip(RoundedCornerShape(3.dp))
      .background(color))
    Text(label, color = PokedexColors.TextDim, fontSize = 12.sp)
  }
}

@Composable
private fun SectionLabel(text: String) {
  Text(
    text.uppercase(),
    color = PokedexColors.TextFaint,
    fontSize = 12.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 1.8.sp
  )
}

@Composable
private fun PickerOverlay(
  open: TeamPickerUiState.Open,
  onQueryChange: (String) -> Unit,
  onSelect: (Pokemon) -> Unit,
  onRemove: () -> Unit,
  onClose: () -> Unit,
) {
  val searchFocus = remember { FocusRequester() }
  LaunchedEffect(open.slot) { searchFocus.requestFocus() }
  Box(
    Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.6f))
      .clickable(onClick = onClose),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth(0.6f)
        .fillMaxHeight(0.8f)
        .clip(RoundedCornerShape(20.dp))
        .background(PokedexColors.SurfaceRaised)
        .border(1.dp, PokedexColors.Line, RoundedCornerShape(20.dp))
        .clickable(enabled = false, onClick = {})
        .padding(28.dp),
    ) {
      Text(
        text = if (open.replacing) stringResource(
          R.string.team_picker_title_replace,
          open.slot + 1
        ) else stringResource(R.string.team_picker_title_add, open.slot + 1),
        color = PokedexColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold,
      )
      Spacer(Modifier.height(16.dp))
      TvSearchField(
        value = open.query,
        onValueChange = onQueryChange,
        onClear = { onQueryChange("") },
        hint = stringResource(R.string.team_picker_hint),
        accent = TeamAccent,
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(searchFocus)
      )
      Spacer(Modifier.height(18.dp))
      Box(Modifier
        .weight(1f)
        .fillMaxWidth()) {
        when (val r = open.results) {
          PickerResults.Idle -> CenteredMessage(stringResource(R.string.team_picker_prompt))
          PickerResults.Loading -> CenteredMessage(stringResource(R.string.team_picker_prompt))
          PickerResults.Empty -> CenteredMessage(
            stringResource(
              R.string.team_picker_empty,
              open.query
            )
          )

          PickerResults.Error -> CenteredMessage(stringResource(R.string.team_picker_error))
          is PickerResults.Results -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items = r.items, key = { it.id }) { p -> PickerRow(p, onSelect) }
          }
        }
      }
      if (open.replacing) {
        Spacer(Modifier.height(12.dp))
        Text(
          text = stringResource(R.string.team_picker_remove),
          color = WeakOutline, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
          modifier = Modifier
            .clickable(onClick = onRemove)
            .padding(8.dp),
        )
      }
    }
  }
}

@Composable
private fun PickerRow(pokemon: Pokemon, onSelect: (Pokemon) -> Unit) {
  val interaction = remember { MutableInteractionSource() }
  val focused by interaction.collectIsFocusedAsState()
  val accent = pokemon.types.first().color()
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(PokedexColors.Surface)
      .tvFocusRing(focused = focused, accent = accent, cornerRadius = 12.dp)
      .clickable(interactionSource = interaction, indication = null) { onSelect(pokemon) }
      .focusable(interactionSource = interaction)
      .padding(10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    PokemonSprite(
      spriteUrl = pokemon.spriteUrl,
      contentDescription = stringResource(R.string.sprite_cd, pokemon.name),
      modifier = Modifier.size(44.dp)
    )
    Text(
      pokemon.name,
      color = PokedexColors.TextPrimary,
      fontSize = 15.sp,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.weight(1f)
    )
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
      pokemon.types.forEach {
        TypeBadge(
          it,
          size = TypeBadgeSize.SM
        )
      }
    }
  }
}

@Composable
private fun CenteredMessage(text: String) {
  Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text, color = PokedexColors.TextDim, fontSize = 14.sp)
  }
}
