package io.beanthemoonman.pokeapp.domain.model

import io.beanthemoonman.pokeapp.domain.model.ItemCategory.Companion.fromApiName


/**
 * A dictionary item. Mirrors the PokéAPI `item` resource fields the UI needs:
 * cost, category, the short effect line, and a flavor description.
 */
data class Item(
  val id: Int,
  val name: String,
  val category: ItemCategory,
  val cost: Int,
  val shortEffect: String,
  val flavor: String,
  val spriteUrl: String,
)

/**
 * Display buckets for the items list filter chips. PokéAPI has ~50 granular item
 * categories; each maps into one of these coarse, user-facing groups (best-effort —
 * see [fromApiName]). Unmapped categories fall into [OTHER] so nothing is lost.
 *
 * [slug] is a stable id (used for persistence); [apiNames] are the PokéAPI
 * `item-category` names that roll up into this bucket.
 */
enum class ItemCategory(val slug: String, val apiNames: Set<String>) {
  POKE_BALLS("poke-balls", setOf("standard-balls", "special-balls", "apricorn-balls")),
  HEALING("healing", setOf("healing", "revival", "status-cures", "picky-healing", "in-a-pinch")),
  MEDICINE("medicine", setOf("medicine", "vitamins", "pp-recovery", "stat-boosts", "nature-mints")),
  EVOLUTION("evolution", setOf("evolution", "species-candies")),
  HELD_ITEMS(
    "held-items",
    setOf(
      "held-items",
      "choice",
      "bad-held-items",
      "type-enhancement",
      "type-protection",
      "scarves",
      "jewels",
      "plates",
      "memories",
      "effort-training"
    )
  ),
  MEGA_STONES("mega-stones", setOf("mega-stones", "z-crystals", "dynamax-crystals")),
  KEY_ITEMS(
    "key-items",
    setOf(
      "key-items",
      "plot-advancement",
      "gameplay",
      "event-items",
      "dex-completion",
      "spelunking",
      "data-cards"
    )
  ),
  OTHER("other", emptySet());

  companion object {
    /** Resolves a PokéAPI category name to its display bucket; unknown → [OTHER]. */
    fun fromApiName(name: String): ItemCategory =
      entries.firstOrNull { name in it.apiNames } ?: OTHER

    /** Resolves a persisted [slug] back to the enum; unknown → [OTHER]. */
    fun fromSlug(slug: String): ItemCategory =
      entries.firstOrNull { it.slug == slug } ?: OTHER

    /** Filter chips, in display order. `null` (All) is handled by the caller. */
    val chips: List<ItemCategory> = listOf(
      POKE_BALLS, HEALING, MEDICINE, EVOLUTION, HELD_ITEMS, MEGA_STONES, KEY_ITEMS,
    )
  }
}
