package io.beanthemoonman.pokeapp.ui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.beanthemoonman.pokeapp.domain.model.Generation
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.ui.common.theme.TypeOnLight
import io.beanthemoonman.pokeapp.ui.common.theme.accentColor

/**
 * Header pill showing the active generation; tap to re-open the root selector.
 * Derived from the VersionChip spec in wireframes/components.jsx.
 */
@Composable
fun VersionChip(
    generation: Generation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = generation.accentColor()
    val shape = RoundedCornerShape(percent = 50)
    Row(
        modifier = modifier
            .clip(shape)
            .background(accent.copy(alpha = 0.14f))
            .border(1.dp, accent.copy(alpha = 0.4f), shape)
            .clickable(onClick = onClick)
            .padding(start = 7.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            modifier = Modifier
                .size(17.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.5f)))),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = generation.label, color = TypeOnLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = generation.region,
            color = PokedexColors.TextPrimary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(text = "›", color = PokedexColors.TextDim, fontSize = 14.sp)
    }
}
