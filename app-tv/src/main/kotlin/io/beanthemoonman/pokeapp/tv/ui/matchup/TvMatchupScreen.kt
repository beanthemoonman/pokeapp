package io.beanthemoonman.pokeapp.tv.ui.matchup

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.type.DefenseBucket
import io.beanthemoonman.pokeapp.tv.R
import io.beanthemoonman.pokeapp.tv.ui.common.TvGenBlock
import io.beanthemoonman.pokeapp.tv.ui.common.TvHints
import io.beanthemoonman.pokeapp.tv.ui.common.TvNavItem
import io.beanthemoonman.pokeapp.tv.ui.common.TvScreenScaffold
import io.beanthemoonman.pokeapp.tv.ui.common.TvSectionLabel
import io.beanthemoonman.pokeapp.tv.ui.common.TvSidebar
import io.beanthemoonman.pokeapp.tv.ui.common.tvFocusRing
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadge
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadgeSize
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.ui.common.theme.color
import io.beanthemoonman.pokeapp.uistate.typecalc.TypeMatchupData
import io.beanthemoonman.pokeapp.uistate.typecalc.TypeMatchupViewModel

/** TV Type Matchup — defensive calculator (tv-screens.jsx `TVMatchup`). */
@Composable
fun TvMatchupScreen(
    onNavigate: (TvNavItem) -> Unit,
    onSwitchGeneration: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TypeMatchupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val data = (state as? UiState.Success)?.data

    TvScreenScaffold(
        active = TvNavItem.MATCHUP,
        onNavigate = onNavigate,
        modifier = modifier,
        sidebar = {
            TvSidebar {
                Column {
                    TvSectionLabel(stringResource(R.string.matchup_sidebar_title))
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.matchup_sidebar_body), color = PokedexColors.TextDim, fontSize = 12.5.sp, lineHeight = 20.sp)
                }
                data?.let { TvGenBlock(it.generation, onSwitchGeneration) }
            }
        },
    ) {
        if (data == null) {
            Box(Modifier.fillMaxSize())
            return@TvScreenScaffold
        }
        Column {
            Text(stringResource(R.string.matchup_title), color = PokedexColors.TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp)
            Text(
                text = stringResource(R.string.matchup_subtitle, data.roster.size, data.generation.label),
                color = PokedexColors.TextFaint, fontSize = 12.5.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 4.dp),
            )
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(26.dp)) {
            // Left — defender hero + picker
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                DefenderHero(data.defenders)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    TvSectionLabel(stringResource(R.string.matchup_choose))
                    if (data.defending) {
                        Text(stringResource(R.string.matchup_clear), color = PokedexColors.TextDim, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = viewModel::clearDefenders).padding(4.dp))
                    }
                }
                TypePickerGrid(roster = data.roster, selected = data.defenders, onToggle = viewModel::toggleDefender)
            }
            // Right — grouped results
            ResultsColumn(data, Modifier.weight(1.35f).fillMaxHeight())
        }
        TvHints(listOf(stringResource(R.string.matchup_hint_pick), stringResource(R.string.matchup_hint_toggle), stringResource(R.string.matchup_hint_scroll)))
    }
}

@Composable
private fun DefenderHero(defenders: List<Type>) {
    val accent = defenders.firstOrNull()?.color() ?: PokedexColors.TextFaint
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = if (defenders.isEmpty()) 0.10f else 0.3f))
            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TvSectionLabel(stringResource(R.string.matchup_defenders_label))
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (defenders.isEmpty()) stringResource(R.string.matchup_prompt) else defenders.joinToString(" / ") { it.name },
            color = PokedexColors.TextPrimary, fontSize = if (defenders.isEmpty()) 14.sp else 26.sp, fontWeight = FontWeight.Bold,
        )
        if (defenders.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { defenders.forEach { TypeBadge(it, size = TypeBadgeSize.MD) } }
        }
    }
}

@Composable
private fun TypePickerGrid(roster: List<Type>, selected: List<Type>, onToggle: (Type) -> Unit) {
    LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items = roster, key = { it.name }) { type ->
            val on = type in selected
            val interaction = remember { MutableInteractionSource() }
            val focused by interaction.collectIsFocusedAsState()
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .tvFocusRing(focused = focused, accent = type.color(), cornerRadius = 999.dp)
                    .clickable(interactionSource = interaction, indication = null) { onToggle(type) }
                    .focusable(interactionSource = interaction),
                contentAlignment = Alignment.Center,
            ) {
                TypeBadge(type, size = TypeBadgeSize.SM, soft = !on, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private data class BucketMeta(val titleRes: Int, val multRes: Int, val color: Color)

private fun DefenseBucket.meta(): BucketMeta = when (this) {
    DefenseBucket.QUAD -> BucketMeta(R.string.matchup_group_quad, R.string.matchup_mult_quad, Color(0xFFFF5C5C))
    DefenseBucket.DOUBLE -> BucketMeta(R.string.matchup_group_double, R.string.matchup_mult_double, Color(0xFFE0712F))
    DefenseBucket.NEUTRAL -> BucketMeta(R.string.matchup_group_neutral, R.string.matchup_mult_neutral, Color(0xFF9AA0AC))
    DefenseBucket.HALF -> BucketMeta(R.string.matchup_group_half, R.string.matchup_mult_half, Color(0xFF62C24A))
    DefenseBucket.QUARTER -> BucketMeta(R.string.matchup_group_quarter, R.string.matchup_mult_quarter, Color(0xFF3FA98F))
    DefenseBucket.IMMUNE -> BucketMeta(R.string.matchup_group_immune, R.string.matchup_mult_immune, Color(0xFF7A6BB0))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultsColumn(data: TypeMatchupData, modifier: Modifier = Modifier) {
    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (data.defending) {
            Text(
                text = stringResource(R.string.matchup_every_attacker, data.defenders.joinToString(" / ") { it.name }),
                color = PokedexColors.TextDim, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            )
        }
        DefenseBucket.entries.forEach { bucket ->
            val members = data.defenseGroups[bucket].orEmpty()
            val meta = bucket.meta()
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 9.dp)) {
                    Box(Modifier.size(width = 4.dp, height = 14.dp).clip(RoundedCornerShape(2.dp)).background(meta.color))
                    Text(stringResource(meta.titleRes), color = PokedexColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(meta.multRes), color = meta.color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.weight(1f))
                    Text(members.size.toString(), color = PokedexColors.TextFaint, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
                if (members.isEmpty()) {
                    Text(stringResource(R.string.matchup_group_none), color = PokedexColors.TextFaint, fontSize = 12.sp, fontStyle = FontStyle.Italic)
                } else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        members.forEach { TypeBadge(it, size = TypeBadgeSize.SM) }
                    }
                }
            }
        }
    }
}
