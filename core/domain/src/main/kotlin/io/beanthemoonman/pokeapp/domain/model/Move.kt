package io.beanthemoonman.pokeapp.domain.model

/**
 * A dictionary move. Mirrors the PokéAPI `move` resource fields the UI needs:
 * type, damage class (category), power/accuracy/pp, the introduction generation,
 * and the short effect line.
 *
 * Unlike items, moves ARE generation-scoped: [generationId] is the generation the
 * move was introduced in, used to scope the dictionary to "through gen X".
 *
 * [type] is nullable for the rare moves PokéAPI exposes with a non-battle type
 * (e.g. "unknown"); the UI renders those without a [TypeBadge].
 */
data class Move(
    val id: Int,
    val name: String,
    val type: Type?,
    val category: MoveCategory,
    val power: Int?,
    val accuracy: Int?,
    val pp: Int,
    val generationId: Int,
    val shortEffect: String,
)
