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
}
