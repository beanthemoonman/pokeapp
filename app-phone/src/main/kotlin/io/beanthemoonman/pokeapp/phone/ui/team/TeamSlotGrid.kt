package io.beanthemoonman.pokeapp.phone.ui.team

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.model.Stats
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.phone.R
import io.beanthemoonman.pokeapp.ui.common.component.PokemonSprite
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexTheme
import io.beanthemoonman.pokeapp.ui.common.theme.color

/**
 * The six team slots laid out in a 2-column × 3-row grid. Stateless: a filled slot renders the
 * sprite, name, a type-color stripe and a remove affordance; an empty slot renders a dashed add
 * tile. Tapping anywhere on a slot calls [onSlotClick] with its index.
 */
@Composable
fun TeamSlotGrid(
    team: List<Pokemon?>,
    onSlotClick: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(SLOT_GAP)) {
        team.chunked(COLUMNS).forEachIndexed { rowIndex, row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SLOT_GAP)) {
                row.forEachIndexed { colIndex, pokemon ->
                    val index = rowIndex * COLUMNS + colIndex
                    TeamSlot(
                        index = index,
                        pokemon = pokemon,
                        onSlotClick = onSlotClick,
                        onRemove = onRemove,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamSlot(
    index: Int,
    pokemon: Pokemon?,
    onSlotClick: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    if (pokemon == null) {
        EmptySlot(modifier = modifier, shape = shape, onClick = { onSlotClick(index) })
    } else {
        FilledSlot(index = index, pokemon = pokemon, shape = shape, onSlotClick = onSlotClick, onRemove = onRemove, modifier = modifier)
    }
}

@Composable
private fun EmptySlot(
    modifier: Modifier,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .border(1.5.dp, PokedexColors.Line, shape)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = PokedexColors.TextFaint,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = stringResource(R.string.team_slot_add),
            color = PokedexColors.TextFaint,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun FilledSlot(
    index: Int,
    pokemon: Pokemon,
    shape: RoundedCornerShape,
    onSlotClick: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier,
) {
    val accent = pokemon.types.firstOrNull()?.color() ?: PokedexColors.TextDim
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(
                Brush.linearGradient(listOf(accent.copy(alpha = 0.16f), PokedexColors.Surface)),
            )
            .border(1.dp, accent.copy(alpha = 0.3f), shape)
            .clickable { onSlotClick(index) }
            .padding(10.dp),
    ) {
        Text(
            text = "#%03d".format(pokemon.id),
            color = accent.copy(alpha = 0.8f),
            fontSize = 9.5.sp,
            modifier = Modifier.align(Alignment.TopStart),
        )

        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = stringResource(R.string.team_slot_remove),
            tint = PokedexColors.TextDim,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(percent = 50))
                .clickable { onRemove(index) }
                .padding(2.dp)
                .size(16.dp),
        )

        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            PokemonSprite(
                spriteUrl = pokemon.spriteUrl,
                contentDescription = pokemon.name,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = pokemon.name,
                color = PokedexColors.TextPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp),
            )
            Row(modifier = Modifier.padding(top = 5.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                pokemon.types.forEach { type ->
                    Box(
                        modifier = Modifier
                            .size(width = 18.dp, height = 5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(type.color()),
                    )
                }
            }
        }
    }
}

private const val COLUMNS = 2
private val SLOT_GAP = 10.dp

@Preview(widthDp = 360, backgroundColor = 0xFF0C0D11, showBackground = true)
@Composable
private fun TeamSlotGridPreview() {
    fun sample(id: Int, name: String, types: List<Type>) =
        Pokemon(id, name, "", types, Stats(1, 1, 1, 1, 1, 1), 0, 0)
    PokedexTheme {
        Box(modifier = Modifier.background(Color(0xFF0C0D11)).padding(16.dp)) {
            TeamSlotGrid(
                team = listOf(
                    sample(3, "Venusaur", listOf(Type.GRASS, Type.POISON)),
                    sample(6, "Charizard", listOf(Type.FIRE, Type.FLYING)),
                    sample(9, "Blastoise", listOf(Type.WATER)),
                    null, null, null,
                ),
                onSlotClick = {},
                onRemove = {},
            )
        }
    }
}
