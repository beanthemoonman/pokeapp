package io.beanthemoonman.pokeapp.ui.common.theme

import androidx.compose.ui.graphics.Color
import io.beanthemoonman.pokeapp.domain.model.Generation
import io.beanthemoonman.pokeapp.domain.model.Type

/**
 * Placeholder per-generation accent, drawn from the existing type-color tokens
 * (matches the accents chosen in wireframes/data.js). Not an official brand color.
 */
fun Generation.accentColor(): Color = when (id) {
    1 -> Type.FIRE
    2 -> Type.ELECTRIC
    3 -> Type.WATER
    4 -> Type.ICE
    5 -> Type.DARK
    6 -> Type.FAIRY
    7 -> Type.PSYCHIC
    8 -> Type.FIGHTING
    else -> Type.DRAGON
}.color()
