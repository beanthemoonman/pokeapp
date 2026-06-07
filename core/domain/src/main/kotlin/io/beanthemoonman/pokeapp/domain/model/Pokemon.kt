package io.beanthemoonman.pokeapp.domain.model

/**
 * Clean domain model for a Pokémon. No Android or persistence concerns.
 */
data class Pokemon(
    val id: Int,
    val name: String,
    val spriteUrl: String,
    val types: List<Type>,
    val stats: Stats,
    val height: Int,
    val weight: Int
)

data class Stats(
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val specialAttack: Int,
    val specialDefense: Int,
    val speed: Int
) {
    val total: Int
        get() = hp + attack + defense + specialAttack + specialDefense + speed
}

enum class Type {
    NORMAL, FIRE, WATER, GRASS, ELECTRIC, ICE, FIGHTING, POISON,
    GROUND, FLYING, PSYCHIC, BUG, ROCK, GHOST, DRAGON, DARK, STEEL, FAIRY
}
