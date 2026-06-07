package io.beanthemoonman.pokeapp.ui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.ui.common.theme.color
import io.beanthemoonman.pokeapp.ui.common.theme.onColor

enum class TypeBadgeSize { SM, MD, LG }

/**
 * Pill-shaped type badge. Type-color fill (or soft tinted variant), uppercase label.
 * Derived from the TypeBadge spec in wireframes/components.jsx.
 */
@Composable
fun TypeBadge(
    type: Type,
    modifier: Modifier = Modifier,
    size: TypeBadgeSize = TypeBadgeSize.MD,
    soft: Boolean = false,
) {
    val accent = type.color()
    val (padding, fontSize) = when (size) {
        TypeBadgeSize.SM -> PaddingValues(horizontal = 8.dp, vertical = 3.dp) to 10.sp
        TypeBadgeSize.MD -> PaddingValues(horizontal = 11.dp, vertical = 4.dp) to 11.5.sp
        TypeBadgeSize.LG -> PaddingValues(horizontal = 15.dp, vertical = 6.dp) to 14.sp
    }

    val shape = RoundedCornerShape(percent = 50)
    val base = if (soft) {
        modifier
            .clip(shape)
            .background(accent.copy(alpha = 0.16f))
            .border(1.dp, accent.copy(alpha = 0.4f), shape)
    } else {
        modifier
            .clip(shape)
            .background(accent)
    }

    Text(
        text = type.name,
        modifier = base.padding(padding),
        color = if (soft) accent else type.onColor(),
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.06.em(fontSize),
    )
}

/** Letter-spacing helper: wireframe uses .06em; Compose wants sp. */
private fun Double.em(fontSize: androidx.compose.ui.unit.TextUnit) =
    (this * fontSize.value).sp
