package io.beanthemoonman.pokeapp.ui.common.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.beanthemoonman.pokeapp.ui.common.theme.PokedexColors

/**
 * Horizontal stat bar: label · value · colored fill. The fill animates from 0 to its
 * target width on first composition. Derived from the StatBar spec in
 * wireframes/components.jsx (grid 52 / 34 / 1fr, 8dp track, .6s ease).
 */
@Composable
fun StatBar(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier,
    maxValue: Int = 255,
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(value) { started = true }

    val target = (value.toFloat() / maxValue).coerceIn(0.03f, 1f)
    val fraction by animateFloatAsState(
        targetValue = if (started) target else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "stat-fill",
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.width(52.dp),
            color = PokedexColors.TextDim,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = value.toString(),
            modifier = Modifier.width(34.dp).padding(end = 10.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.07f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .layout { measurable, constraints ->
                        val w = (constraints.maxWidth * fraction).toInt()
                        val placeable = measurable.measure(
                            constraints.copy(minWidth = w, maxWidth = w)
                        )
                        layout(w, placeable.height) { placeable.place(0, 0) }
                    }
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}
