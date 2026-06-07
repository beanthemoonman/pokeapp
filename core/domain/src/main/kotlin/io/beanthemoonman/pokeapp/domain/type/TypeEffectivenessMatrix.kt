package io.beanthemoonman.pokeapp.domain.type

import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.domain.model.Type.BUG
import io.beanthemoonman.pokeapp.domain.model.Type.DARK
import io.beanthemoonman.pokeapp.domain.model.Type.DRAGON
import io.beanthemoonman.pokeapp.domain.model.Type.ELECTRIC
import io.beanthemoonman.pokeapp.domain.model.Type.FAIRY
import io.beanthemoonman.pokeapp.domain.model.Type.FIGHTING
import io.beanthemoonman.pokeapp.domain.model.Type.FIRE
import io.beanthemoonman.pokeapp.domain.model.Type.FLYING
import io.beanthemoonman.pokeapp.domain.model.Type.GHOST
import io.beanthemoonman.pokeapp.domain.model.Type.GRASS
import io.beanthemoonman.pokeapp.domain.model.Type.GROUND
import io.beanthemoonman.pokeapp.domain.model.Type.ICE
import io.beanthemoonman.pokeapp.domain.model.Type.NORMAL
import io.beanthemoonman.pokeapp.domain.model.Type.POISON
import io.beanthemoonman.pokeapp.domain.model.Type.PSYCHIC
import io.beanthemoonman.pokeapp.domain.model.Type.ROCK
import io.beanthemoonman.pokeapp.domain.model.Type.STEEL
import io.beanthemoonman.pokeapp.domain.model.Type.WATER

/**
 * Static type effectiveness chart (modern / Gen VI+). Hardcoded — never fetched from the API.
 *
 * Only non-1× matchups are listed; any pair not present defaults to 1×.
 * Source of truth: wireframes/data.js CHART.
 */
object TypeEffectivenessMatrix {

    // attacking -> (defending -> multiplier), exceptions only
    private val chart: Map<Type, Map<Type, Float>> = mapOf(
        NORMAL to mapOf(ROCK to 0.5f, GHOST to 0f, STEEL to 0.5f),
        FIRE to mapOf(FIRE to 0.5f, WATER to 0.5f, GRASS to 2f, ICE to 2f, BUG to 2f, ROCK to 0.5f, DRAGON to 0.5f, STEEL to 2f),
        WATER to mapOf(FIRE to 2f, WATER to 0.5f, GRASS to 0.5f, GROUND to 2f, ROCK to 2f, DRAGON to 0.5f),
        ELECTRIC to mapOf(WATER to 2f, ELECTRIC to 0.5f, GRASS to 0.5f, GROUND to 0f, FLYING to 2f, DRAGON to 0.5f),
        GRASS to mapOf(FIRE to 0.5f, WATER to 2f, GRASS to 0.5f, POISON to 0.5f, GROUND to 2f, FLYING to 0.5f, BUG to 0.5f, ROCK to 2f, DRAGON to 0.5f, STEEL to 0.5f),
        ICE to mapOf(FIRE to 0.5f, WATER to 0.5f, GRASS to 2f, ICE to 0.5f, GROUND to 2f, FLYING to 2f, DRAGON to 2f, STEEL to 0.5f),
        FIGHTING to mapOf(NORMAL to 2f, ICE to 2f, POISON to 0.5f, FLYING to 0.5f, PSYCHIC to 0.5f, BUG to 0.5f, ROCK to 2f, GHOST to 0f, DARK to 2f, STEEL to 2f, FAIRY to 0.5f),
        POISON to mapOf(GRASS to 2f, POISON to 0.5f, GROUND to 0.5f, ROCK to 0.5f, GHOST to 0.5f, STEEL to 0f, FAIRY to 2f),
        GROUND to mapOf(FIRE to 2f, ELECTRIC to 2f, GRASS to 0.5f, POISON to 2f, FLYING to 0f, BUG to 0.5f, ROCK to 2f, STEEL to 2f),
        FLYING to mapOf(ELECTRIC to 0.5f, GRASS to 2f, FIGHTING to 2f, BUG to 2f, ROCK to 0.5f, STEEL to 0.5f),
        PSYCHIC to mapOf(FIGHTING to 2f, POISON to 2f, PSYCHIC to 0.5f, DARK to 0f, STEEL to 0.5f),
        BUG to mapOf(FIRE to 0.5f, GRASS to 2f, FIGHTING to 0.5f, POISON to 0.5f, FLYING to 0.5f, PSYCHIC to 2f, GHOST to 0.5f, DARK to 2f, STEEL to 0.5f, FAIRY to 0.5f),
        ROCK to mapOf(FIRE to 2f, ICE to 2f, FIGHTING to 0.5f, GROUND to 0.5f, FLYING to 2f, BUG to 2f, STEEL to 0.5f),
        GHOST to mapOf(NORMAL to 0f, PSYCHIC to 2f, GHOST to 2f, DARK to 0.5f),
        DRAGON to mapOf(DRAGON to 2f, STEEL to 0.5f, FAIRY to 0f),
        DARK to mapOf(FIGHTING to 0.5f, PSYCHIC to 2f, GHOST to 2f, DARK to 0.5f, FAIRY to 0.5f),
        STEEL to mapOf(FIRE to 0.5f, WATER to 0.5f, ELECTRIC to 0.5f, ICE to 2f, ROCK to 2f, STEEL to 0.5f, FAIRY to 2f),
        FAIRY to mapOf(FIRE to 0.5f, FIGHTING to 2f, POISON to 0.5f, DRAGON to 2f, DARK to 2f, STEEL to 0.5f),
    )

    /** Single-type effectiveness multiplier: 0f, 0.5f, 1f, or 2f. */
    fun effectiveness(attacking: Type, defending: Type): Float =
        chart[attacking]?.get(defending) ?: 1f

    /** Combined multiplier of an attacking type against a (mono- or dual-type) defender. */
    fun attackingEffectiveness(attacking: Type, defendingTypes: List<Type>): Float =
        defendingTypes.fold(1f) { acc, def -> acc * effectiveness(attacking, def) }

    /** For a defending type combo, the incoming multiplier from every attacking type. */
    fun defendingWeaknesses(defendingTypes: List<Type>): Map<Type, Float> =
        Type.entries.associateWith { attacking -> attackingEffectiveness(attacking, defendingTypes) }
}
