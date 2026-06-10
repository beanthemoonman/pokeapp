package io.beanthemoonman.pokeapp.phone.ui.typecalc

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import io.beanthemoonman.pokeapp.domain.type.DefenseBucket
import io.beanthemoonman.pokeapp.phone.R
import io.beanthemoonman.pokeapp.ui.common.component.SkeletonBox
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadge
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadgeSize
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.ui.common.theme.color
import io.beanthemoonman.pokeapp.uistate.typecalc.TypeMatchupData
import io.beanthemoonman.pokeapp.uistate.typecalc.TypeMatchupViewModel

@Composable
fun TypeMatchupScreen(
  modifier: Modifier = Modifier,
  viewModel: TypeMatchupViewModel = hiltViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  Box(modifier = modifier
    .fillMaxSize()
    .background(PokedexColors.Background)) {
    when (val s = state) {
      is UiState.Loading -> MatchupLoading()
      is UiState.Success -> MatchupContent(
        data = s.data,
        onDefenderToggled = viewModel::toggleDefender,
        onDefendersCleared = viewModel::clearDefenders,
      )

      is UiState.Error -> MatchupError(s.message)
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MatchupContent(
  data: TypeMatchupData,
  onDefenderToggled: (Type) -> Unit,
  onDefendersCleared: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.height(4.dp))
    Text(
      text = stringResource(R.string.typecalc_title),
      color = PokedexColors.TextPrimary,
      fontSize = 26.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = (-0.02).em(26.sp),
    )
    Text(
      text = stringResource(
        R.string.typecalc_subtitle,
        data.roster.size,
        data.generation.label,
      ),
      color = PokedexColors.TextFaint,
      fontSize = 11.5.sp,
      fontFamily = FontFamily.Monospace,
      modifier = Modifier.padding(top = 2.dp),
    )

    Spacer(Modifier.height(16.dp))
    DefenderHero(defenders = data.defenders)

    Spacer(Modifier.height(20.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
      SectionLabel(stringResource(R.string.typecalc_pick_defender))
      Spacer(Modifier.weight(1f))
      if (data.defending) {
        Text(
          text = stringResource(R.string.typecalc_clear),
          color = PokedexColors.TextDim,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.clickable { onDefendersCleared() },
        )
      }
    }
    Spacer(Modifier.height(10.dp))
    TypeChipRow(
      roster = data.roster,
      isSelected = { it in data.defenders },
      onClick = onDefenderToggled,
    )

    Spacer(Modifier.height(22.dp))
    if (data.defending) {
      ResultModeLabel(stringResource(R.string.typecalc_mode_defense, data.defenders.comboName()))
      Spacer(Modifier.height(14.dp))
      DefenseBucket.entries.forEach { bucket ->
        GroupSection(
          accent = bucket.accent(),
          title = stringResource(bucket.titleRes()),
          mult = stringResource(bucket.multRes()),
          types = data.defenseGroups[bucket].orEmpty(),
        )
        Spacer(Modifier.height(16.dp))
      }
    } else {
      EmptyPrompt()
    }
    Spacer(Modifier.height(8.dp))
  }
}

/** A wrapping strip of selectable type chips: selected = filled badge, others = soft. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypeChipRow(
  roster: List<Type>,
  isSelected: (Type) -> Boolean,
  onClick: (Type) -> Unit,
) {
  FlowRow(
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    roster.forEach { type ->
      TypeBadge(
        type = type,
        size = TypeBadgeSize.SM,
        soft = !isSelected(type),
        modifier = Modifier.clickable { onClick(type) },
      )
    }
  }
}

/**
 * The defending-type hero card. Empty renders the dashed "pick a type" placeholder; otherwise
 * the chosen combo's badges over an accent gradient drawn from the first type.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DefenderHero(defenders: List<Type>) {
  Column {
    PickerLabel(stringResource(R.string.typecalc_defender))
    Spacer(Modifier.height(8.dp))
    if (defenders.isEmpty()) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 96.dp)
          .clip(RoundedCornerShape(14.dp))
          .border(1.5.dp, PokedexColors.Line, RoundedCornerShape(14.dp))
          .padding(vertical = 18.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        Text(
          text = stringResource(R.string.typecalc_defender_empty_title),
          color = PokedexColors.TextDim,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
          text = stringResource(R.string.typecalc_defender_empty_hint),
          color = PokedexColors.TextFaint,
          fontSize = 11.sp,
        )
      }
      return@Column
    }
    val accent = defenders.first().color()
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 96.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(
          Brush.linearGradient(listOf(accent.copy(alpha = 0.3f), PokedexColors.Surface))
        )
        .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
        .padding(vertical = 20.dp, horizontal = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        text = defenders.comboName(),
        color = PokedexColors.TextPrimary,
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
      )
      Spacer(Modifier.height(10.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        defenders.forEach { TypeBadge(type = it, size = TypeBadgeSize.SM) }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroupSection(accent: Color, title: String, mult: String, types: List<Type>) {
  Column {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(width = 4.dp, height = 14.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(accent),
      )
      Spacer(Modifier.width(8.dp))
      Text(
        text = title,
        color = PokedexColors.TextPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
      )
      Spacer(Modifier.width(8.dp))
      Text(
        text = mult,
        color = accent,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.Monospace,
      )
      Spacer(Modifier.weight(1f))
      Text(
        text = types.size.toString(),
        color = PokedexColors.TextFaint,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
      )
    }
    Spacer(Modifier.height(9.dp))
    if (types.isEmpty()) {
      Text(
        text = stringResource(R.string.typecalc_group_empty),
        color = PokedexColors.TextFaint,
        fontSize = 12.sp,
      )
    } else {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        types.forEach { TypeBadge(type = it, size = TypeBadgeSize.SM) }
      }
    }
  }
}

@Composable
private fun EmptyPrompt() {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = stringResource(R.string.typecalc_prompt),
      color = PokedexColors.TextDim,
      fontSize = 13.sp,
      fontWeight = FontWeight.SemiBold,
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(4.dp))
    Text(
      text = stringResource(R.string.typecalc_prompt_hint),
      color = PokedexColors.TextFaint,
      fontSize = 12.sp,
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun ResultModeLabel(text: String) {
  Text(
    text = text,
    color = PokedexColors.TextDim,
    fontSize = 12.sp,
    fontWeight = FontWeight.SemiBold,
  )
}

@Composable
private fun SectionLabel(text: String) {
  Text(
    text = text,
    color = PokedexColors.TextFaint,
    fontSize = 11.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.12.em(11.sp),
  )
}

@Composable
private fun PickerLabel(text: String) {
  Text(
    text = text,
    color = PokedexColors.TextFaint,
    fontSize = 10.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.14.em(10.sp),
    textAlign = TextAlign.Center,
    modifier = Modifier.fillMaxWidth(),
  )
}

@Composable
private fun MatchupLoading() {
  Column(modifier = Modifier
    .fillMaxSize()
    .padding(16.dp)) {
    SkeletonBox(modifier = Modifier
      .width(180.dp)
      .height(30.dp))
    Spacer(Modifier.height(20.dp))
    SkeletonBox(modifier = Modifier
      .fillMaxWidth()
      .height(96.dp))
    Spacer(Modifier.height(24.dp))
    repeat(4) {
      SkeletonBox(modifier = Modifier
        .fillMaxWidth()
        .height(56.dp))
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun MatchupError(message: String) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text = stringResource(R.string.typecalc_error),
      color = PokedexColors.TextPrimary,
      fontSize = 16.sp,
      fontWeight = FontWeight.SemiBold,
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(6.dp))
    Text(
      text = message,
      color = PokedexColors.TextFaint,
      fontSize = 12.sp,
      textAlign = TextAlign.Center,
    )
  }
}

// ── Display tokens ────────────────────────────────────────────────────────────

/** "Fire / Flying" — a type-combo label. */
private fun List<Type>.comboName(): String =
  joinToString(" / ") { it.displayName() }

// Defensive bucket colors — weakness = warm/red (bad for defender), resist = green, immune = purple.
private fun DefenseBucket.accent(): Color = when (this) {
  DefenseBucket.QUAD -> Color(0xFFFF5C5C)
  DefenseBucket.DOUBLE -> Color(0xFFE0712F)
  DefenseBucket.NEUTRAL -> Color(0xFF9AA0AC)
  DefenseBucket.HALF -> Color(0xFF62C24A)
  DefenseBucket.QUARTER -> Color(0xFF3FA98F)
  DefenseBucket.IMMUNE -> Color(0xFF7A6BB0)
}

@StringRes
private fun DefenseBucket.titleRes(): Int = when (this) {
  DefenseBucket.QUAD -> R.string.typecalc_def_quad
  DefenseBucket.DOUBLE -> R.string.typecalc_def_double
  DefenseBucket.NEUTRAL -> R.string.typecalc_def_neutral
  DefenseBucket.HALF -> R.string.typecalc_def_half
  DefenseBucket.QUARTER -> R.string.typecalc_def_quarter
  DefenseBucket.IMMUNE -> R.string.typecalc_def_immune
}

@StringRes
private fun DefenseBucket.multRes(): Int = when (this) {
  DefenseBucket.QUAD -> R.string.typecalc_def_mult_quad
  DefenseBucket.DOUBLE -> R.string.typecalc_def_mult_double
  DefenseBucket.NEUTRAL -> R.string.typecalc_def_mult_neutral
  DefenseBucket.HALF -> R.string.typecalc_def_mult_half
  DefenseBucket.QUARTER -> R.string.typecalc_def_mult_quarter
  DefenseBucket.IMMUNE -> R.string.typecalc_def_mult_immune
}

/** "FIRE" → "Fire" for hero titles and combo labels. */
private fun Type.displayName(): String =
  name.lowercase().replaceFirstChar { it.uppercase() }

/** Letter-spacing helper mirroring TypeBadge: wireframe uses em, Compose wants sp. */
private fun Double.em(fontSize: androidx.compose.ui.unit.TextUnit) =
  (this * fontSize.value).sp
