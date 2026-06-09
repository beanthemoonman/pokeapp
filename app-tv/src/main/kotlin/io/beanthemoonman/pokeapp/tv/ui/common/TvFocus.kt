package io.beanthemoonman.pokeapp.tv.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The wireframe `.pdx-focused` D-pad ring (see tv-screens.jsx): a 2px accent border that
 * lifts and slightly scales the focused element. Applied to any focusable tile so the
 * focus position is always clearly visible against the dark background.
 */
@Composable
fun Modifier.tvFocusRing(
    focused: Boolean,
    accent: Color,
    cornerRadius: Dp = 14.dp,
    focusedScale: Float = 1.03f,
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (focused) focusedScale else 1f,
        label = "tv-focus-scale",
    )
    return this
        .scale(scale)
        .border(
            width = if (focused) 2.dp else 1.dp,
            color = if (focused) accent else Color.White.copy(alpha = 0.10f),
            shape = RoundedCornerShape(cornerRadius),
        )
}
