package io.beanthemoonman.pokeapp.phone.ui.detail

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import io.beanthemoonman.pokeapp.domain.model.EvolutionStage
import io.beanthemoonman.pokeapp.domain.model.MoveCategory
import io.beanthemoonman.pokeapp.domain.model.MoveInfo
import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.model.PokemonDetail
import io.beanthemoonman.pokeapp.domain.model.Stats
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.phone.R
import io.beanthemoonman.pokeapp.ui.common.component.PokemonSprite
import io.beanthemoonman.pokeapp.ui.common.component.SkeletonBox
import io.beanthemoonman.pokeapp.ui.common.component.StatBar
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadge
import io.beanthemoonman.pokeapp.ui.common.component.TypeBadgeSize
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.ui.common.theme.color

@Composable
fun PokemonDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PokemonDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DetailContent(
        state = state,
        onBack = onBack,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@Composable
private fun DetailContent(
    state: UiState<PokemonDetail>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().background(PokedexColors.Background)) {
        BackBar(onBack = onBack)
        when (state) {
            is UiState.Loading -> DetailLoading()
            is UiState.Error -> DetailError(onRetry = onRetry)
            is UiState.Success -> DetailLoaded(detail = state.data)
        }
    }
}

@Composable
private fun BackBar(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(PokedexColors.Surface)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.detail_back),
                tint = PokedexColors.TextPrimary,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun DetailLoaded(detail: PokemonDetail) {
    val pokemon = detail.pokemon
    val accent = pokemon.types.firstOrNull()?.color() ?: Type.FIRE.color()
    var tab by rememberSaveable { mutableStateOf(DetailTab.STATS) }

    HeaderCard(pokemon = pokemon, accent = accent)
    SegmentedTabs(selected = tab, accent = accent, onSelect = { tab = it })
    when (tab) {
        DetailTab.STATS -> StatsPane(stats = pokemon.stats, accent = accent)
        DetailTab.MOVES -> MovesPane(moves = detail.moves)
        DetailTab.ABOUT -> AboutPane(detail = detail, accent = accent)
        DetailTab.EVO -> EvoPane(detail = detail)
    }
}

@Composable
private fun HeaderCard(pokemon: Pokemon, accent: Color) {
    Row(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 14.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(listOf(accent.copy(alpha = 0.14f), PokedexColors.Surface))
            )
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PokemonSprite(
            spriteUrl = pokemon.spriteUrl,
            contentDescription = stringResource(R.string.sprite_cd, pokemon.name),
            modifier = Modifier
                .size(92.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.12f)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dexNumber(pokemon.id),
                color = PokedexColors.TextFaint,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = pokemon.name,
                color = PokedexColors.TextPrimary,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                pokemon.types.forEach { TypeBadge(type = it, size = TypeBadgeSize.SM) }
            }
        }
    }
}

@Composable
private fun SegmentedTabs(selected: DetailTab, accent: Color, onSelect: (DetailTab) -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PokedexColors.Surface)
            .border(1.dp, PokedexColors.Line, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DetailTab.entries.forEach { entry ->
            val on = entry == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (on) accent.copy(alpha = 0.18f) else Color.Transparent)
                    .then(
                        if (on) Modifier.border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(9.dp))
                        else Modifier
                    )
                    .clickable { onSelect(entry) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(entry.label),
                    color = if (on) accent else PokedexColors.TextFaint,
                    fontSize = 12.5.sp,
                    fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                )
            }
        }
    }
}

// ── Stats ────────────────────────────────────────────────────────────────────

@Composable
private fun StatsPane(stats: Stats, accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel(text = stringResource(R.string.detail_stats_title))
            Text(
                text = stats.total.toString(),
                color = PokedexColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
        }
        statRows(stats).forEach { (labelRes, value) ->
            StatBar(label = stringResource(labelRes), value = value, color = accent)
        }
    }
}

private fun statRows(s: Stats): List<Pair<Int, Int>> = listOf(
    R.string.detail_stat_hp to s.hp,
    R.string.detail_stat_attack to s.attack,
    R.string.detail_stat_defense to s.defense,
    R.string.detail_stat_spatk to s.specialAttack,
    R.string.detail_stat_spdef to s.specialDefense,
    R.string.detail_stat_speed to s.speed,
)

// ── Moves ────────────────────────────────────────────────────────────────────

@Composable
private fun MovesPane(moves: List<MoveInfo>) {
    if (moves.isEmpty()) {
        CenteredMessage(text = stringResource(R.string.detail_moves_empty))
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp),
    ) {
        items(items = moves, key = { it.name }) { move ->
            MoveRow(move)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .height(1.dp)
                    .background(PokedexColors.Line),
            )
        }
    }
}

@Composable
private fun MoveRow(move: MoveInfo) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = if (move.level > 0) {
                stringResource(R.string.detail_move_level, move.level)
            } else {
                stringResource(R.string.detail_move_level_evo)
            },
            color = PokedexColors.TextFaint,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(42.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = move.name,
                color = PokedexColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.padding(top = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                move.type?.let { TypeBadge(type = it, size = TypeBadgeSize.SM) }
                Text(
                    text = stringResource(move.category.label),
                    color = move.category.color,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = move.power?.toString() ?: stringResource(R.string.detail_value_none),
                color = PokedexColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
            )
            val accuracy = move.accuracy?.let { stringResource(R.string.detail_accuracy, it) }
                ?: stringResource(R.string.detail_value_none)
            Text(
                text = stringResource(R.string.detail_move_meta, accuracy, move.pp),
                color = PokedexColors.TextFaint,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

// ── About ────────────────────────────────────────────────────────────────────

@Composable
private fun AboutPane(detail: PokemonDetail, accent: Color) {
    val pokemon = detail.pokemon
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp),
    ) {
        if (detail.flavorText.isNotEmpty()) {
            Text(
                text = detail.flavorText,
                color = PokedexColors.TextDim,
                fontSize = 14.sp,
                lineHeight = 22.sp,
            )
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                AboutBlock(
                    label = stringResource(R.string.detail_about_height),
                    value = stringResource(R.string.detail_height_value, formatTenths(pokemon.height)),
                )
                AboutBlock(
                    label = stringResource(R.string.detail_about_category),
                    value = detail.genus.ifEmpty { stringResource(R.string.detail_value_none) },
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                AboutBlock(
                    label = stringResource(R.string.detail_about_weight),
                    value = stringResource(R.string.detail_weight_value, formatTenths(pokemon.weight)),
                )
                AboutBlock(
                    label = stringResource(R.string.detail_about_abilities),
                    value = detail.abilities.joinToString(" · ").ifEmpty {
                        stringResource(R.string.detail_value_none)
                    },
                )
            }
        }
        CatchRateCard(captureRate = detail.captureRate, accent = accent)
    }
}

@Composable
private fun AboutBlock(label: String, value: String) {
    Column {
        SectionLabel(text = label)
        Text(
            text = value,
            color = PokedexColors.TextPrimary,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 5.dp),
        )
    }
}

@Composable
private fun CatchRateCard(captureRate: Int, accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 22.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PokedexColors.Surface)
            .border(1.dp, PokedexColors.Line, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SectionLabel(text = stringResource(R.string.detail_catch_rate))
            Text(
                text = stringResource(R.string.detail_catch_rate_value, captureRate),
                color = PokedexColors.TextDim,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.07f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((captureRate / 255f).coerceIn(0.02f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent),
            )
        }
    }
}

// ── Evolution ─────────────────────────────────────────────────────────────────

@Composable
private fun EvoPane(detail: PokemonDetail) {
    val stages = detail.evolution
    if (stages.size <= 1) {
        CenteredMessage(text = stringResource(R.string.detail_evo_none, detail.pokemon.name))
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        stages.forEachIndexed { index, stage ->
            EvoNode(stage)
            if (index < stages.lastIndex) {
                EvoConnector(condition = stages[index + 1].condition)
            }
        }
    }
}

@Composable
private fun EvoNode(stage: EvolutionStage) {
    val accent = stage.types.firstOrNull()?.color() ?: PokedexColors.TextFaint
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PokemonSprite(
            spriteUrl = stage.spriteUrl,
            contentDescription = stringResource(R.string.sprite_cd, stage.name),
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.12f)),
        )
        Text(
            text = stage.name,
            color = PokedexColors.TextPrimary,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = dexNumber(stage.id),
            color = PokedexColors.TextFaint,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun EvoConnector(condition: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 28.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = PokedexColors.TextFaint,
            modifier = Modifier.size(20.dp),
        )
        if (!condition.isNullOrEmpty()) {
            Text(
                text = condition,
                color = PokedexColors.TextDim,
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

// ── Shared ────────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = PokedexColors.TextFaint,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
    )
}

@Composable
private fun CenteredMessage(text: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = PokedexColors.TextDim,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DetailLoading() {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header skeleton.
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBox(modifier = Modifier.size(92.dp), cornerRadius = 14.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SkeletonBox(modifier = Modifier.width(48.dp).height(10.dp), cornerRadius = 4.dp)
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.7f).height(22.dp), cornerRadius = 6.dp)
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.4f).height(18.dp), cornerRadius = 999.dp)
            }
        }
        SkeletonBox(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(44.dp),
            cornerRadius = 12.dp,
        )
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            repeat(6) {
                SkeletonBox(modifier = Modifier.fillMaxWidth().height(14.dp), cornerRadius = 4.dp)
            }
        }
    }
}

@Composable
private fun DetailError(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PokedexColors.SurfaceRaised),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = Type.FIRE.color(),
                modifier = Modifier.size(30.dp),
            )
        }
        Text(
            text = stringResource(R.string.detail_error_title),
            color = PokedexColors.TextPrimary,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.detail_error_body),
            color = PokedexColors.TextDim,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Type.FIRE.color(),
                contentColor = PokedexColors.Background,
            ),
        ) {
            Text(text = stringResource(R.string.detail_retry), fontWeight = FontWeight.Bold)
        }
    }
}

/** Segmented-tab identities, in display order. */
private enum class DetailTab(@param:StringRes val label: Int) {
    STATS(R.string.detail_tab_stats),
    MOVES(R.string.detail_tab_moves),
    ABOUT(R.string.detail_tab_about),
    EVO(R.string.detail_tab_evo),
}

/** Wireframe move-category accent colors (catColor in phone-detail.jsx). */
private val MoveCategory.color: Color
    get() = when (this) {
        MoveCategory.PHYSICAL -> Color(0xFFE0712F)
        MoveCategory.SPECIAL -> Color(0xFF5C8BD6)
        MoveCategory.STATUS -> Color(0xFF9AA0AC)
    }

private val MoveCategory.label: Int
    get() = when (this) {
        MoveCategory.PHYSICAL -> R.string.detail_cat_physical
        MoveCategory.SPECIAL -> R.string.detail_cat_special
        MoveCategory.STATUS -> R.string.detail_cat_status
    }

/** Formats a national dex id as a zero-padded `#001` label. */
private fun dexNumber(id: Int): String = "#%03d".format(id)

/** PokéAPI height/weight come in tenths (decimetres / hectograms) → "1.7". */
private fun formatTenths(value: Int): String = "%.1f".format(value / 10.0)
