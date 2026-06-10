package io.beanthemoonman.pokeapp.ui.common.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Animated shimmer rectangle for loading states. Mirrors the `.pdx-skel` keyframe in
 * wireframes/components.jsx: a 90° white gradient (4% → 9% → 4%) sweeping across.
 */
@Composable
fun SkeletonBox(
  modifier: Modifier = Modifier,
  cornerRadius: androidx.compose.ui.unit.Dp = 8.dp,
) {
  val transition = rememberInfiniteTransition(label = "skeleton")
  val translate by transition.animateFloat(
    initialValue = -2f,
    targetValue = 2f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1400),
      repeatMode = RepeatMode.Restart,
    ),
    label = "skeleton-translate",
  )

  val base = Color.White.copy(alpha = 0.04f)
  val highlight = Color.White.copy(alpha = 0.09f)
  val shift = translate * 400f
  val brush = Brush.linearGradient(
    colors = listOf(base, highlight, base),
    start = Offset(shift, 0f),
    end = Offset(shift + 400f, 0f),
  )

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(cornerRadius))
      .background(brush)
  )
}
