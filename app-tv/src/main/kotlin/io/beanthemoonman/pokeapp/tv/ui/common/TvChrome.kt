package io.beanthemoonman.pokeapp.tv.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Backpack
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CatchingPokemon
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.beanthemoonman.pokeapp.domain.model.Generation
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.tv.R
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.ui.common.theme.accentColor
import io.beanthemoonman.pokeapp.ui.common.theme.color

/** Accent used as the app's primary chrome color (the Pokédex/Fire theme). */
val TvFireAccent: Color = Type.FIRE.color()

/** The five top-level leanback destinations, mirroring the phone bottom nav. */
enum class TvNavItem(val labelRes: Int, val icon: ImageVector) {
    DEX(R.string.nav_dex, Icons.Outlined.CatchingPokemon),
    ITEMS(R.string.nav_items, Icons.Outlined.Backpack),
    MOVES(R.string.nav_moves, Icons.Outlined.Bolt),
    TEAM(R.string.nav_team, Icons.Outlined.Groups),
    MATCHUP(R.string.nav_matchup, Icons.Outlined.Shield),
}

/**
 * Persistent left navigation rail — replaces the phone bottom nav on TV (see tv-screens.jsx
 * `TVNavRail`). Each item is D-pad focusable; pressing center invokes [onSelect].
 */
@Composable
fun TvNavRail(
    active: TvNavItem,
    onSelect: (TvNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(84.dp)
            .fillMaxHeight()
            .background(PokedexColors.Surface)
            .border(width = 1.dp, color = PokedexColors.Line)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TvBrand()
        Spacer(Modifier.height(18.dp))
        TvNavItem.entries.forEach { item ->
            TvNavRailItem(item = item, active = item == active, onSelect = { onSelect(item) })
        }
    }
}

@Composable
private fun TvBrand() {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(TvFireAccent),
        contentAlignment = Alignment.Center,
    ) {
        Text("P", color = PokedexColors.Background, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun TvNavRailItem(item: TvNavItem, active: Boolean, onSelect: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Column(
        modifier = Modifier
            .width(64.dp)
            .clip(RoundedCornerShape(13.dp))
            .then(
                if (active) Modifier.background(Color.White.copy(alpha = 0.10f))
                else Modifier
            )
            .tvFocusRing(focused = focused, accent = TvFireAccent, cornerRadius = 13.dp)
            .clickable(interactionSource = interaction, indication = null, onClick = onSelect)
            .focusable(interactionSource = interaction)
            .padding(vertical = 11.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = if (active || focused) PokedexColors.TextPrimary else PokedexColors.TextFaint,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = stringResource(item.labelRes),
            color = if (active || focused) PokedexColors.TextPrimary else PokedexColors.TextFaint,
            fontSize = 9.5.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

/**
 * Standard list-screen shell: nav rail · optional filter sidebar · content column.
 * Mirrors the layout shared by `TVBrowse`/`TVItems`/`TVMoves`/`TVTeam`/`TVMatchup`.
 */
@Composable
fun TvScreenScaffold(
    active: TvNavItem,
    onNavigate: (TvNavItem) -> Unit,
    modifier: Modifier = Modifier,
    sidebar: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Row(modifier = modifier.fillMaxSize().background(PokedexColors.Background)) {
        TvNavRail(active = active, onSelect = onNavigate)
        sidebar?.invoke()
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(start = 36.dp, end = 36.dp, top = 26.dp, bottom = 18.dp),
        ) {
            content()
        }
    }
}

/** Filter-sidebar container (232dp). Holds filter sections + the generation block. */
@Composable
fun TvSidebar(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .width(232.dp)
            .fillMaxHeight()
            .background(PokedexColors.Surface)
            .border(width = 1.dp, color = PokedexColors.Line)
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp),
        content = content,
    )
}

/** A single selectable filter option (type / category / damage-class chip-row). */
data class TvFilterOption(val id: String, val label: String, val dot: Color)

/** A vertical chip-list filter section (Type / Category / Damage Class). */
@Composable
fun TvFilterSection(
    title: String,
    options: List<TvFilterOption>,
    activeId: String,
    onSelect: (String) -> Unit,
) {
    Column {
        TvSectionLabel(title)
        Spacer(Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            options.forEach { option ->
                val on = option.id == activeId
                val interaction = remember { MutableInteractionSource() }
                val focused by interaction.collectIsFocusedAsState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(9.dp))
                        .then(if (on) Modifier.background(option.dot.copy(alpha = 0.16f)) else Modifier)
                        .tvFocusRing(focused = focused, accent = option.dot, cornerRadius = 9.dp)
                        .clickable(interactionSource = interaction, indication = null) { onSelect(option.id) }
                        .focusable(interactionSource = interaction)
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    Box(Modifier.size(9.dp).clip(RoundedCornerShape(3.dp)).background(option.dot))
                    Text(
                        text = option.label,
                        color = if (on || focused) PokedexColors.TextPrimary else PokedexColors.TextDim,
                        fontSize = 13.sp,
                        fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }
    }
}

/** Generation block shown in every filter sidebar. Selecting it re-opens the root selector. */
@Composable
fun TvGenBlock(generation: Generation, onSwitchGeneration: () -> Unit) {
    val accent = generation.accentColor()
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Column {
        TvSectionLabel(stringResource(R.string.sidebar_generation))
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(11.dp))
                .background(accent.copy(alpha = 0.16f))
                .border(1.5.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(11.dp))
                .tvFocusRing(focused = focused, accent = accent, cornerRadius = 11.dp)
                .clickable(interactionSource = interaction, indication = null, onClick = onSwitchGeneration)
                .focusable(interactionSource = interaction)
                .padding(horizontal = 13.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Box(
                modifier = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(accent),
                contentAlignment = Alignment.Center,
            ) {
                Text(generation.label, color = PokedexColors.Background, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Column {
                Text(generation.region, color = PokedexColors.TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = stringResource(R.string.sidebar_dex_range, generation.dexEnd),
                    color = PokedexColors.TextDim,
                    fontSize = 10.5.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.sidebar_menu_change),
            color = PokedexColors.TextFaint,
            fontSize = 10.5.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
fun TvSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = PokedexColors.TextFaint,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.8.sp,
    )
}

/** Content header: title + mono subtitle on the left, optional trailing slot (search). */
@Composable
fun TvContentHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(title, color = PokedexColors.TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp)
            Text(subtitle, color = PokedexColors.TextFaint, fontSize = 12.5.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 4.dp))
        }
        trailing?.invoke()
    }
}

/**
 * Focusable search field styled as the wireframe search pill. Real text entry (D-pad +
 * remote keyboard); shows a clear affordance when non-empty.
 */
@Composable
fun TvSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    hint: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Row(
        modifier = modifier
            .width(280.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PokedexColors.Surface)
            .border(1.dp, if (focused) accent.copy(alpha = 0.5f) else PokedexColors.Line, RoundedCornerShape(12.dp))
            .padding(horizontal = 18.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = PokedexColors.TextFaint, modifier = Modifier.size(18.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(hint, color = PokedexColors.TextFaint, fontSize = 14.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = PokedexColors.TextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                interactionSource = interaction,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = stringResource(R.string.browse_search_clear),
                tint = PokedexColors.TextFaint,
                modifier = Modifier.size(16.dp).clickable(onClick = onClear),
            )
        }
    }
}

/** Footer D-pad hint strip. */
@Composable
fun TvHints(hints: List<String>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        hints.forEach { hint ->
            Text(hint, color = PokedexColors.TextFaint, fontSize = 11.5.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

/** Back-affordance row used at the top of every detail screen. */
@Composable
fun TvBackBar(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(bottom = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, tint = PokedexColors.TextFaint, modifier = Modifier.size(18.dp))
        Text(label, color = PokedexColors.TextFaint, fontSize = 12.5.sp, fontFamily = FontFamily.Monospace)
    }
}

/** Full-panel error state with a focusable Retry button. */
@Composable
fun TvErrorState(
    accent: Color,
    title: String,
    body: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(accent.copy(alpha = 0.12f))
                .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = accent, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text(title, color = PokedexColors.TextPrimary, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(body, color = PokedexColors.TextDim, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(accent)
                .tvFocusRing(focused = focused, accent = accent, cornerRadius = 12.dp)
                .clickable(interactionSource = interaction, indication = null, onClick = onRetry)
                .focusable(interactionSource = interaction)
                .padding(horizontal = 30.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.browse_retry), color = PokedexColors.Background, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}
