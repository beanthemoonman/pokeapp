package io.beanthemoonman.pokeapp.tv.ui.detail

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.beanthemoonman.pokeapp.domain.model.EvolutionStage
import io.beanthemoonman.pokeapp.domain.model.MoveInfo
import io.beanthemoonman.pokeapp.domain.model.PokemonDetail
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.tv.R
import io.beanthemoonman.pokeapp.tv.ui.common.TvBackBar
import io.beanthemoonman.pokeapp.tv.ui.common.TvErrorState
import io.beanthemoonman.pokeapp.tv.ui.common.TvFireAccent
import io.beanthemoonman.pokeapp.tv.ui.common.tvFocusRing
import io.beanthemoonman.pokeapp.ui.common.component.PokemonSprite
import io.beanthemoonman.pokeapp.ui.common.component.StatBar
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadge
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadgeSize
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.uistate.detail.PokemonDetailViewModel
import androidx.compose.ui.res.stringResource

/** TV Pokémon detail — split stats panel + tabbed right pane (see tv-screens.jsx `TVDetail`). */
@Composable
fun TvDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PokemonDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize().background(PokedexColors.Background)) {
        when (val s = state) {
            is UiState.Loading -> Box(Modifier.fillMaxSize().padding(44.dp)) {
                TvBackBar(stringResource(R.string.detail_back))
            }
            is UiState.Error -> TvErrorState(
                accent = TvFireAccent,
                title = stringResource(R.string.detail_error_title),
                body = stringResource(R.string.detail_error_body),
                onRetry = viewModel::retry,
            )
            is UiState.Success -> DetailContent(s.data)
        }
    }
}

@Composable
private fun DetailContent(detail: PokemonDetail) {
    val pokemon = detail.pokemon
    Row(Modifier.fillMaxSize()) {
        // Left — sprite, identity, base stats.
        Column(
            modifier = Modifier
                .fillMaxWidth(0.46f)
                .fillMaxHeight()
                .border(width = 1.dp, color = PokedexColors.Line)
                .padding(44.dp),
        ) {
            TvBackBar(stringResource(R.string.detail_back))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                PokemonSprite(
                    spriteUrl = pokemon.spriteUrl,
                    contentDescription = stringResource(R.string.sprite_cd, pokemon.name),
                    modifier = Modifier.size(150.dp).clip(RoundedCornerShape(20.dp))
                        .background(TvFireAccent.copy(alpha = 0.12f)),
                )
                Column {
                    Text(stringResource(R.string.dex_number, pokemon.id), color = PokedexColors.TextFaint, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                    Text(pokemon.name, color = PokedexColors.TextPrimary, fontSize = 42.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp, modifier = Modifier.padding(vertical = 6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        pokemon.types.forEach { TypeBadge(it, size = TypeBadgeSize.MD) }
                    }
                }
            }
            Spacer(Modifier.height(26.dp))
            TvSectionLabel(stringResource(R.string.detail_stats_title))
            Spacer(Modifier.height(16.dp))
            val stats = pokemon.stats
            val rows = listOf(
                R.string.detail_stat_hp to stats.hp,
                R.string.detail_stat_attack to stats.attack,
                R.string.detail_stat_defense to stats.defense,
                R.string.detail_stat_spatk to stats.specialAttack,
                R.string.detail_stat_spdef to stats.specialDefense,
                R.string.detail_stat_speed to stats.speed,
            )
            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                rows.forEach { (labelRes, value) ->
                    StatBar(label = stringResource(labelRes), value = value, color = TvFireAccent)
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TvSectionLabel(stringResource(R.string.detail_base_total))
                Text(stats.total.toString(), color = PokedexColors.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
        // Right — tabbed detail.
        DetailTabs(detail, Modifier.weight(1f).fillMaxHeight().padding(44.dp))
    }
}

private enum class DetailTab(val labelRes: Int) {
    MOVES(R.string.detail_tab_moves),
    ABOUT(R.string.detail_tab_about),
    EVOLUTION(R.string.detail_tab_evo),
}

@Composable
private fun DetailTabs(detail: PokemonDetail, modifier: Modifier = Modifier) {
    var tab by remember { mutableStateOf(DetailTab.MOVES) }
    Column(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailTab.entries.forEach { t ->
                TabChip(label = stringResource(t.labelRes), active = t == tab, onClick = { tab = t })
            }
        }
        Spacer(Modifier.height(26.dp))
        when (tab) {
            DetailTab.MOVES -> MovesTab(detail.moves)
            DetailTab.ABOUT -> AboutTab(detail)
            DetailTab.EVOLUTION -> EvolutionTab(detail)
        }
    }
}

@Composable
private fun TabChip(label: String, active: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .background(if (active) TvFireAccent.copy(alpha = 0.18f) else PokedexColors.Surface)
            .border(1.dp, if (active) TvFireAccent.copy(alpha = 0.5f) else PokedexColors.Line, RoundedCornerShape(11.dp))
            .tvFocusRing(focused = focused, accent = TvFireAccent, cornerRadius = 11.dp)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .focusable(interactionSource = interaction)
            .padding(horizontal = 22.dp, vertical = 12.dp),
    ) {
        Text(label, color = if (active || focused) PokedexColors.TextPrimary else PokedexColors.TextDim, fontSize = 14.5.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun MovesTab(moves: List<MoveInfo>) {
    if (moves.isEmpty()) {
        Text(stringResource(R.string.detail_moves_empty), color = PokedexColors.TextDim, fontSize = 14.sp)
        return
    }
    LazyColumn(Modifier.fillMaxSize()) {
        items(items = moves, key = { it.name }) { move ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(move.name, color = PokedexColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                move.type?.let { TypeBadge(it, size = TypeBadgeSize.SM) }
                Text(
                    text = move.power?.toString() ?: stringResource(R.string.detail_value_none),
                    color = PokedexColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace,
                )
                Text(
                    text = move.accuracy?.let { stringResource(R.string.detail_accuracy, it) } ?: stringResource(R.string.detail_value_none),
                    color = PokedexColors.TextFaint, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
private fun AboutTab(detail: PokemonDetail) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(detail.flavorText, color = PokedexColors.TextPrimary, fontSize = 16.sp, lineHeight = 24.sp)
        AboutRow(stringResource(R.string.detail_about_category), detail.genus)
        AboutRow(stringResource(R.string.detail_about_height), stringResource(R.string.detail_height_value, formatDecimal(detail.pokemon.height)))
        AboutRow(stringResource(R.string.detail_about_weight), stringResource(R.string.detail_weight_value, formatDecimal(detail.pokemon.weight)))
        AboutRow(stringResource(R.string.detail_about_abilities), detail.abilities.joinToString(", "))
        AboutRow(stringResource(R.string.detail_catch_rate), stringResource(R.string.detail_catch_rate_value, detail.captureRate))
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = PokedexColors.TextFaint, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(value, color = PokedexColors.TextPrimary, fontSize = 14.sp, modifier = Modifier.padding(start = 16.dp))
    }
}

@Composable
private fun EvolutionTab(detail: PokemonDetail) {
    if (detail.evolution.size <= 1) {
        Text(stringResource(R.string.detail_evo_none, detail.pokemon.name), color = PokedexColors.TextDim, fontSize = 14.sp)
        return
    }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items = detail.evolution, key = { it.id }) { stage -> EvolutionRow(stage) }
    }
}

@Composable
private fun EvolutionRow(stage: EvolutionStage) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(PokedexColors.Surface).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PokemonSprite(spriteUrl = stage.spriteUrl, contentDescription = stringResource(R.string.sprite_cd, stage.name), modifier = Modifier.size(56.dp))
        Column(Modifier.weight(1f)) {
            Text(stage.name, color = PokedexColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 4.dp)) {
                stage.types.forEach { TypeBadge(it, size = TypeBadgeSize.SM) }
            }
        }
        stage.condition?.let { Text(it, color = PokedexColors.TextFaint, fontSize = 12.sp, fontFamily = FontFamily.Monospace) }
    }
}

@Composable
private fun TvSectionLabel(text: String) {
    Text(text.uppercase(), color = PokedexColors.TextFaint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
}

/** PokéAPI gives height in decimetres and weight in hectograms — render as a single decimal. */
private fun formatDecimal(value: Int): String = "%.1f".format(value / 10.0)
