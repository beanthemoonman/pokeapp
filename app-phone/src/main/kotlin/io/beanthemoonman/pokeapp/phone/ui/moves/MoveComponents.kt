package io.beanthemoonman.pokeapp.phone.ui.moves

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import io.beanthemoonman.pokeapp.domain.model.MoveCategory
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.phone.R
import io.beanthemoonman.pokeapp.ui.common.theme.color

/** Screen accent for the moves dictionary — the dragon purple (wireframe `typeColor('dragon')`). */
val MovesAccent: Color = Type.DRAGON.color()

/** Dark-on-accent text for filled accent surfaces (wireframe `#15140f`). */
val MovesOnAccent = Color(0xFF15140F)

/** Damage-class colors, shared with the Pokémon detail Moves tab (wireframe `catColor`). */
fun MoveCategory.classColor(): Color = when (this) {
  MoveCategory.PHYSICAL -> Color(0xFFE0712F)
  MoveCategory.SPECIAL -> Color(0xFF5C8BD6)
  MoveCategory.STATUS -> Color(0xFF9AA0AC)
}

/** String resource for a damage class's chip/eyebrow label. */
@StringRes
fun MoveCategory.labelRes(): Int = when (this) {
  MoveCategory.PHYSICAL -> R.string.move_class_physical
  MoveCategory.SPECIAL -> R.string.move_class_special
  MoveCategory.STATUS -> R.string.move_class_status
}

/** Damage-class filter chips, in display order. `null` (All) is handled by the caller. */
val MoveClassChips: List<MoveCategory> = listOf(
  MoveCategory.PHYSICAL, MoveCategory.SPECIAL, MoveCategory.STATUS,
)
