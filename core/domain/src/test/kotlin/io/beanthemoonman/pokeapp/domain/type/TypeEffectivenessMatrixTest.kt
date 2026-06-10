package io.beanthemoonman.pokeapp.domain.type

import io.beanthemoonman.pokeapp.domain.model.Type
import org.junit.Assert.assertEquals
import org.junit.Test

class TypeEffectivenessMatrixTest {

  @Test
  fun `super effective single matchup`() {
    assertEquals(2f, TypeEffectivenessMatrix.effectiveness(Type.WATER, Type.FIRE))
  }

  @Test
  fun `not very effective single matchup`() {
    assertEquals(0.5f, TypeEffectivenessMatrix.effectiveness(Type.FIRE, Type.WATER))
  }

  @Test
  fun `immunity returns zero`() {
    assertEquals(0f, TypeEffectivenessMatrix.effectiveness(Type.NORMAL, Type.GHOST))
    assertEquals(0f, TypeEffectivenessMatrix.effectiveness(Type.GROUND, Type.FLYING))
  }

  @Test
  fun `neutral matchup defaults to one`() {
    assertEquals(1f, TypeEffectivenessMatrix.effectiveness(Type.NORMAL, Type.WATER))
  }

  @Test
  fun `dual type multipliers stack`() {
    // Rock attacking Charizard (Fire/Flying): 2x (fire) * 2x (flying) = 4x
    val charizard = listOf(Type.FIRE, Type.FLYING)
    assertEquals(4f, TypeEffectivenessMatrix.attackingEffectiveness(Type.ROCK, charizard))
  }

  @Test
  fun `dual type can cancel to immunity`() {
    // Electric attacking Gyarados (Water/Flying): 2x (water) * ... flying is 1x -> 2x.
    // Ground attacking a Flying type is 0x; stays 0 regardless of partner.
    val partFlying = listOf(Type.GROUND, Type.FLYING)
    assertEquals(0f, TypeEffectivenessMatrix.attackingEffectiveness(Type.ELECTRIC, partFlying))
  }

  @Test
  fun `defending weaknesses covers all attacking types`() {
    val weaknesses = TypeEffectivenessMatrix.defendingWeaknesses(listOf(Type.GRASS, Type.POISON))
    assertEquals(Type.entries.size, weaknesses.size)
    // Grass/Poison is 4x weak to Psychic (psychic 2x poison, 1x grass) -> actually 2x.
    assertEquals(2f, weaknesses[Type.PSYCHIC])
    // Grass/Poison double weak to Fire? Fire 2x grass, 1x poison -> 2x.
    assertEquals(2f, weaknesses[Type.FIRE])
  }

  // ── Per-generation behaviour (values verified against PokéAPI past_damage_relations) ──

  @Test
  fun `generation rosters have the right sizes`() {
    assertEquals(15, TypeEffectivenessMatrix.typesForGeneration(1).size)
    assertEquals(17, TypeEffectivenessMatrix.typesForGeneration(3).size)
    assertEquals(18, TypeEffectivenessMatrix.typesForGeneration(6).size)
  }

  @Test
  fun `gen 1 historical matchups differ from modern`() {
    assertEquals(2f, TypeEffectivenessMatrix.effectiveness(Type.BUG, Type.POISON, 1))
    assertEquals(2f, TypeEffectivenessMatrix.effectiveness(Type.POISON, Type.BUG, 1))
    assertEquals(0f, TypeEffectivenessMatrix.effectiveness(Type.GHOST, Type.PSYCHIC, 1))
    assertEquals(1f, TypeEffectivenessMatrix.effectiveness(Type.ICE, Type.FIRE, 1))
    // Unchanged-within-roster matchup stays the same.
    assertEquals(2f, TypeEffectivenessMatrix.effectiveness(Type.FIRE, Type.GRASS, 1))
  }

  @Test
  fun `types absent from a generation resolve to neutral`() {
    // Fairy doesn't exist before Gen VI; Dark/Steel don't exist in Gen I.
    assertEquals(1f, TypeEffectivenessMatrix.effectiveness(Type.STEEL, Type.FAIRY, 3))
    assertEquals(1f, TypeEffectivenessMatrix.effectiveness(Type.DARK, Type.GHOST, 1))
  }

  @Test
  fun `steel resisted ghost and dark before gen 6`() {
    assertEquals(0.5f, TypeEffectivenessMatrix.effectiveness(Type.GHOST, Type.STEEL, 3))
    assertEquals(0.5f, TypeEffectivenessMatrix.effectiveness(Type.DARK, Type.STEEL, 3))
    // Removed in Gen VI.
    assertEquals(1f, TypeEffectivenessMatrix.effectiveness(Type.GHOST, Type.STEEL, 6))
    assertEquals(1f, TypeEffectivenessMatrix.effectiveness(Type.DARK, Type.STEEL, 6))
  }

  @Test
  fun `gen 2-5 uses modern values where unchanged`() {
    // The Gen-1-only Bug/Poison and Ghost/Psychic quirks are already gone by Gen II.
    assertEquals(0.5f, TypeEffectivenessMatrix.effectiveness(Type.BUG, Type.POISON, 3))
    assertEquals(2f, TypeEffectivenessMatrix.effectiveness(Type.GHOST, Type.PSYCHIC, 3))
  }
}
