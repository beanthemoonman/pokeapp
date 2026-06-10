package io.beanthemoonman.pokeapp.domain.type

/**
 * Buckets the *incoming* multiplier a defending type combo takes from an attacking type.
 * A dual-type defender stacks two matchups, so beyond 0/½/1/2 the values ×4 and ×¼ become
 * possible. Declared worst-to-best for the defender so a [Map] keyed by this enum renders
 * most-dangerous first.
 */
enum class DefenseBucket(val multiplier: Float) {
  QUAD(4f),
  DOUBLE(2f),
  NEUTRAL(1f),
  HALF(0.5f),
  QUARTER(0.25f),
  IMMUNE(0f);

  companion object {
    /** The bucket a stacked defending multiplier (0, ¼, ½, 1, 2, or 4) belongs to. */
    fun forMultiplier(multiplier: Float): DefenseBucket = when {
      multiplier >= 4f -> QUAD
      multiplier >= 2f -> DOUBLE
      multiplier == 0f -> IMMUNE
      multiplier <= 0.25f -> QUARTER
      multiplier < 1f -> HALF
      else -> NEUTRAL
    }
  }
}
