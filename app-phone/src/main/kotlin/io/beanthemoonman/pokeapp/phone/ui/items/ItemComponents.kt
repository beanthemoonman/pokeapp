package io.beanthemoonman.pokeapp.phone.ui.items

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backpack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.beanthemoonman.pokeapp.domain.model.ItemCategory
import io.beanthemoonman.pokeapp.phone.R

/** Neutral gold accent — items have no type color (wireframe `ITEM_ACCENT`). */
val ItemAccent = Color(0xFFC9A24A)

/** Dark-on-gold text for filled gold surfaces (wireframe `#15140f`). */
val ItemOnAccent = Color(0xFF15140F)

/** String resource for a category's chip/eyebrow label. */
@StringRes
fun ItemCategory.labelRes(): Int = when (this) {
    ItemCategory.POKE_BALLS -> R.string.item_cat_balls
    ItemCategory.HEALING -> R.string.item_cat_healing
    ItemCategory.MEDICINE -> R.string.item_cat_medicine
    ItemCategory.EVOLUTION -> R.string.item_cat_evolution
    ItemCategory.HELD_ITEMS -> R.string.item_cat_held
    ItemCategory.MEGA_STONES -> R.string.item_cat_mega
    ItemCategory.KEY_ITEMS -> R.string.item_cat_key
    ItemCategory.OTHER -> R.string.item_cat_other
}

/** Square gold sprite-placeholder tile with a bag glyph (wireframe `ItemIcon`). */
@Composable
fun ItemIcon(
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    cornerRadius: Dp = 10.dp,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.radialGradient(
                    colors = listOf(ItemAccent.copy(alpha = 0.16f), Color.White.copy(alpha = 0.02f)),
                )
            )
            .border(1.dp, ItemAccent.copy(alpha = 0.28f), shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Backpack,
            contentDescription = null,
            tint = ItemAccent,
            modifier = Modifier.size(size * 0.42f),
        )
    }
}
