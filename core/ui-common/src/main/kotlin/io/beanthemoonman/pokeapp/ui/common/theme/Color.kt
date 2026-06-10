package io.beanthemoonman.pokeapp.ui.common.theme

import androidx.compose.ui.graphics.Color
import io.beanthemoonman.pokeapp.domain.model.Type

/**
 * Surface + text tokens. Values derived 1:1 from wireframes/foundations.jsx.
 */
object PokedexColors {
  val Background = Color(0xFF0C0D11)      // bg
  val Surface = Color(0xFF15171D)         // surface
  val SurfaceRaised = Color(0xFF1D2027)   // surfaceRaised
  val TextPrimary = Color(0xFFEEF0F4)     // textPrimary
  val TextDim = Color(0xFF9AA0AC)         // textDim
  val TextFaint = Color(0xFF5F6571)       // textFaint
  val Line = Color(0x1AFFFFFF)            // hairline dividers (white @ 10%)
}

/**
 * Type accent tokens, one per [Type]. Values derived from wireframes/data.js.
 * `dark` types need dark label text for contrast (see [Type.onColor]).
 */
object TypeColors {
  val Normal = Color(0xFF9B9B6E)
  val Fire = Color(0xFFFF7A33)
  val Water = Color(0xFF4F90F0)
  val Electric = Color(0xFFF7CF2E)
  val Grass = Color(0xFF62C24A)
  val Ice = Color(0xFF74CEC0)
  val Fighting = Color(0xFFD6435A)
  val Poison = Color(0xFFB04AC3)
  val Ground = Color(0xFFE0B575)
  val Flying = Color(0xFF9C8BF4)
  val Psychic = Color(0xFFFB5584)
  val Bug = Color(0xFFA6B91A)
  val Rock = Color(0xFFC2B255)
  val Ghost = Color(0xFF7A6BB0)
  val Dragon = Color(0xFF7A4CF0)
  val Dark = Color(0xFF6E5848)
  val Steel = Color(0xFF9FB0C9)
  val Fairy = Color(0xFFEC8FC5)
}

/** Dark label color used on light ("dark:true") type badges. */
val TypeOnLight = Color(0xFF15140F)

fun Type.color(): Color = when (this) {
  Type.NORMAL -> TypeColors.Normal
  Type.FIRE -> TypeColors.Fire
  Type.WATER -> TypeColors.Water
  Type.ELECTRIC -> TypeColors.Electric
  Type.GRASS -> TypeColors.Grass
  Type.ICE -> TypeColors.Ice
  Type.FIGHTING -> TypeColors.Fighting
  Type.POISON -> TypeColors.Poison
  Type.GROUND -> TypeColors.Ground
  Type.FLYING -> TypeColors.Flying
  Type.PSYCHIC -> TypeColors.Psychic
  Type.BUG -> TypeColors.Bug
  Type.ROCK -> TypeColors.Rock
  Type.GHOST -> TypeColors.Ghost
  Type.DRAGON -> TypeColors.Dragon
  Type.DARK -> TypeColors.Dark
  Type.STEEL -> TypeColors.Steel
  Type.FAIRY -> TypeColors.Fairy
}

/** Label/contrast color to use on top of [color]. Light types use dark text. */
fun Type.onColor(): Color = when (this) {
  Type.ELECTRIC, Type.ICE, Type.GROUND, Type.ROCK, Type.STEEL -> TypeOnLight
  else -> Color.White
}
