package io.beanthemoonman.pokeapp.domain.model

/**
 * Rich detail aggregate for the Pokémon detail screen. Composes the base [Pokemon]
 * (stats/types/sprite — already cached for the list) with the extra data the detail
 * tabs need: species text (About), level-up [moves] (Moves), and the [evolution] line.
 */
data class PokemonDetail(
  val pokemon: Pokemon,
  val genus: String,
  val flavorText: String,
  val abilities: List<AbilityInfo>,
  val captureRate: Int,
  val moves: List<MoveInfo>,
  val evolution: List<EvolutionStage>,
  val megaForms: List<MegaForm> = emptyList(),
)

/**
 * A single ability a Pokémon can have. [shortEffect] is the English one-line summary from
 * PokéAPI `ability.effect_entries.short_effect` (blank when the API has none). [isHidden] marks
 * the Pokémon's Hidden Ability slot.
 */
data class AbilityInfo(
  val name: String,
  val shortEffect: String,
  val isHidden: Boolean,
)

/** Damage class of a move, mirroring PokéAPI `move.damage_class`. */
enum class MoveCategory { PHYSICAL, SPECIAL, STATUS }

/**
 * A single move a Pokémon can learn. [power]/[accuracy] are null for moves the API
 * leaves blank (e.g. status moves, or variable-power moves). [level] is the level it is
 * learned at by level-up; 0 means it is gained on evolution rather than at a set level.
 */
data class MoveInfo(
  val name: String,
  val type: Type?,
  val category: MoveCategory,
  val power: Int?,
  val accuracy: Int?,
  val pp: Int,
  val level: Int,
)

/**
 * One node in the evolution tree. [condition] is a short human label for how the
 * previous stage evolves into this one (e.g. "Lv. 16", "Fire Stone"); null for the
 * base stage. [evolvesTo] holds the next stages this one can become — more than one
 * entry means a branching line (e.g. Poliwhirl → Poliwrath / Politoed). The list is
 * empty for a final stage.
 */
data class EvolutionStage(
  val id: Int,
  val name: String,
  val spriteUrl: String,
  val types: List<Type>,
  val condition: String?,
  val evolvesTo: List<EvolutionStage> = emptyList(),
)

/**
 * A Mega Evolution (or Primal) variant of a Pokémon. These are not part of the species
 * evolution chain (PokéAPI models them as alternate forms), so they are surfaced as a
 * separate branch below the line on the detail screen's Evolution tab.
 */
data class MegaForm(
  val id: Int,
  val name: String,
  val spriteUrl: String,
  val types: List<Type>,
)
