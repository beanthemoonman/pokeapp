package io.beanthemoonman.pokeapp.ui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.beanthemoonman.pokeapp.domain.model.Generation
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors
import io.beanthemoonman.pokeapp.ui.common.theme.TypeOnLight
import io.beanthemoonman.pokeapp.ui.common.theme.accentColor

/**
 * Root version-selector tile. Region crest (roman numeral on accent), generation
 * label, region, cumulative dex range, and the bundled game versions.
 * Derived from the GenerationCard spec in wireframes/components.jsx.
 */
@Composable
fun GenerationCard(
    generation: Generation,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val accent = generation.accentColor()
    val shape = RoundedCornerShape(16.dp)
    val container = if (selected) {
        Modifier.background(Brush.linearGradient(listOf(accent.copy(alpha = 0.2f), PokedexColors.Surface)))
    } else {
        Modifier.background(PokedexColors.Surface)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(container)
            .border(1.dp, if (selected) accent.copy(alpha = 0.5f) else PokedexColors.Line, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.radialGradient(listOf(accent, accent.copy(alpha = 0.35f)))),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = generation.label,
                color = TypeOnLight,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "GENERATION ${generation.label}",
                color = accent,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
            )
            Text(
                text = generation.region,
                color = PokedexColors.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
            )
            Text(
                text = "#1–${generation.dexEnd} · ${generation.dexEnd} entries",
                color = PokedexColors.TextDim,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = generation.versions.joinToString(" · "),
                color = PokedexColors.TextFaint,
                fontSize = 11.sp,
                maxLines = 1,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        Text(text = "›", color = if (selected) accent else PokedexColors.TextFaint, fontSize = 22.sp)
    }
}
