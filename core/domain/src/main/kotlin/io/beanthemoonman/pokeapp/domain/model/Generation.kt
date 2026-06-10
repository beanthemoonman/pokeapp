package io.beanthemoonman.pokeapp.domain.model

/**
 * A Pokémon generation — the global context the user picks at the root selector.
 * The dex it opens is the cumulative National Dex `1..dexEnd`, and the era's type
 * chart is selected by [id] (see TypeEffectivenessMatrix).
 */
data class Generation(
  val id: Int,            // 1..9
  val label: String,      // roman numeral, "I".."IX"
  val region: String,     // main region introduced that generation
  val dexEnd: Int,        // last National Dex number available through this gen
  val versions: List<String>,
) {
  val dexRange: IntRange get() = 1..dexEnd
}

/**
 * Static catalog of all generations. Hardcoded reference data — not fetched.
 * Mirrors `GENERATIONS` in wireframes/data.js.
 */
object Generations {
  val all: List<Generation> = listOf(
    Generation(1, "I", "Kanto", 151, listOf("Red", "Blue", "Yellow")),
    Generation(2, "II", "Johto", 251, listOf("Gold", "Silver", "Crystal")),
    Generation(
      3,
      "III",
      "Hoenn",
      386,
      listOf("Ruby", "Sapphire", "Emerald", "FireRed", "LeafGreen")
    ),
    Generation(
      4,
      "IV",
      "Sinnoh",
      493,
      listOf("Diamond", "Pearl", "Platinum", "HeartGold", "SoulSilver")
    ),
    Generation(5, "V", "Unova", 649, listOf("Black", "White", "Black 2", "White 2")),
    Generation(6, "VI", "Kalos", 721, listOf("X", "Y", "Omega Ruby", "Alpha Sapphire")),
    Generation(
      7,
      "VII",
      "Alola",
      809,
      listOf("Sun", "Moon", "Ultra Sun", "Ultra Moon", "Let's Go")
    ),
    Generation(
      8,
      "VIII",
      "Galar",
      905,
      listOf("Sword", "Shield", "Brilliant Diamond", "Shining Pearl", "Legends: Arceus")
    ),
    Generation(9, "IX", "Paldea", 1025, listOf("Scarlet", "Violet")),
  )

  val latest: Generation get() = all.last()

  fun byId(id: Int): Generation? = all.firstOrNull { it.id == id }
}
