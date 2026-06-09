package io.beanthemoonman.pokeapp.tv.ui.version

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.beanthemoonman.pokeapp.domain.model.Generation
import io.beanthemoonman.pokeapp.tv.R
import io.beanthemoonman.pokeapp.tv.ui.common.tvFocusRing
import io.beanthemoonman.pokeapp.ui.common.component.GenerationCard
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.ui.common.theme.accentColor

/** Root leanback generation selector — a D-pad grid of generation cards. */
@Composable
fun TvVersionSelectScreen(
    onGenerationChosen: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TvVersionSelectViewModel = hiltViewModel(),
) {
    val selectedId by viewModel.selectedId.collectAsStateWithLifecycle()
    val firstItem = remember { FocusRequester() }

    LaunchedEffect(Unit) { firstItem.requestFocus() }

    Column(modifier = modifier.fillMaxSize().background(PokedexColors.Background).padding(48.dp)) {
        Header()
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 26.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(viewModel.generations) { index, generation ->
                val focusModifier = if (index == 0) Modifier.focusRequester(firstItem) else Modifier
                FocusableGenerationCard(
                    generation = generation,
                    selected = generation.id == selectedId,
                    onClick = { viewModel.select(generation.id, onGenerationChosen) },
                    modifier = focusModifier,
                )
            }
        }
        Footer()
    }
}

/** [LazyVerticalGrid] has no built-in indexed items helper; thread the index ourselves. */
private inline fun <T> androidx.compose.foundation.lazy.grid.LazyGridScope.itemsIndexed(
    items: List<T>,
    crossinline itemContent: @Composable (index: Int, item: T) -> Unit,
) {
    items(count = items.size, key = { items[it].hashCode() }) { index ->
        itemContent(index, items[index])
    }
}

@Composable
private fun FocusableGenerationCard(
    generation: Generation,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    GenerationCard(
        generation = generation,
        selected = selected || focused,
        onClick = onClick,
        modifier = modifier
            .tvFocusRing(focused = focused, accent = generation.accentColor(), cornerRadius = 16.dp)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .focusable(interactionSource = interaction),
    )
}

@Composable
private fun Header() {
    Column {
        Text(
            text = stringResource(R.string.version_eyebrow).uppercase(),
            color = PokedexColors.TextFaint,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.8.sp,
        )
        Text(
            text = stringResource(R.string.version_title),
            color = PokedexColors.TextPrimary,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun Footer() {
    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        FooterHint(stringResource(R.string.version_hint_select))
        FooterHint(stringResource(R.string.version_hint_confirm))
    }
}

@Composable
private fun FooterHint(text: String) {
    Text(
        text = text,
        color = PokedexColors.TextFaint,
        fontSize = 11.5.sp,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 0.5.sp,
    )
}
