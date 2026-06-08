package io.beanthemoonman.pokeapp.domain.type

import io.beanthemoonman.pokeapp.domain.model.Generations
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
 * Static, per-generation type effectiveness. Hardcoded — never fetched from the API.
 *
 * [chart] is the modern (Gen VI+) baseline; only non-1× matchups are listed and any pair
 * not present defaults to 1×. Earlier eras are the baseline plus the historical overrides
 * in [gen1Overrides] / [gen2to5Overrides], with the type roster narrowed by era
 * (Gen I = 15 types, Gen II–V = 17, Gen VI+ = 18).
 *
 * Override values are transcribed from PokéAPI `type.past_damage_relations` (NOT invented);
 * see wireframes/data.js CHART_OVERRIDES for the same data.
 */
object TypeEffectivenessMatrix {

    // attacking -> (defending -> multiplier), exceptions only — modern (Gen VI+)
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

    // Historical overrides relative to [chart], from PokéAPI past_damage_relations.
    private val gen1Overrides: Map<Type, Map<Type, Float>> = mapOf(
        BUG to mapOf(POISON to 2f),     // Bug ↔ Poison were mutually super-effective
        POISON to mapOf(BUG to 2f),
        GHOST to mapOf(PSYCHIC to 0f),  // Psychic was immune to Ghost (the Gen-I quirk)
        ICE to mapOf(FIRE to 1f),       // Fire did not yet resist Ice
    )
    private val gen2to5Overrides: Map<Type, Map<Type, Float>> = mapOf(
        GHOST to mapOf(STEEL to 0.5f),  // Steel still resisted Ghost & Dark before Gen VI
        DARK to mapOf(STEEL to 0.5f),
    )

    // Type roster by era. Dark/Steel/Fairy are the last three entries in [Type].
    private val gen1Roster: List<Type> = Type.entries - setOf(DARK, STEEL, FAIRY) // 15
    private val gen2to5Roster: List<Type> = Type.entries - setOf(FAIRY)           // 17
    private val modernRoster: List<Type> = Type.entries.toList()                  // 18

    /** Era bucket: 1 = Gen I, 2 = Gen II–V, 6 = Gen VI+. */
    private fun eraOf(generation: Int): Int = when {
        generation >= 6 -> 6
        generation >= 2 -> 2
        else -> 1
    }

    /** Types that exist in the given generation. */
    fun typesForGeneration(generation: Int): List<Type> = when (eraOf(generation)) {
        1 -> gen1Roster
        2 -> gen2to5Roster
        else -> modernRoster
    }

    private fun overridesFor(era: Int): Map<Type, Map<Type, Float>> = when (era) {
        1 -> gen1Overrides
        2 -> gen2to5Overrides
        else -> emptyMap()
    }

    /**
     * Single-type effectiveness multiplier (0f, 0.5f, 1f, or 2f) for a generation.
     * Types outside the generation's roster don't exist there and resolve to 1×.
     */
    fun effectiveness(
        attacking: Type,
        defending: Type,
        generation: Int = Generations.latest.id,
    ): Float {
        val roster = typesForGeneration(generation)
        if (attacking !in roster || defending !in roster) return 1f
        overridesFor(eraOf(generation))[attacking]?.get(defending)?.let { return it }
        return chart[attacking]?.get(defending) ?: 1f
    }

    /** Combined multiplier of an attacking type against a (mono- or dual-type) defender. */
    fun attackingEffectiveness(
        attacking: Type,
        defendingTypes: List<Type>,
        generation: Int = Generations.latest.id,
    ): Float =
        defendingTypes.fold(1f) { acc, def -> acc * effectiveness(attacking, def, generation) }

    /** For a defending type combo, the incoming multiplier from every attacking type in the gen. */
    fun defendingWeaknesses(
        defendingTypes: List<Type>,
        generation: Int = Generations.latest.id,
    ): Map<Type, Float> =
        typesForGeneration(generation)
            .associateWith { attacking -> attackingEffectiveness(attacking, defendingTypes, generation) }
}
