package io.beanthemoonman.pokeapp.domain.model

/**
 * A team's type coverage, computed statically over [io.beanthemoonman.pokeapp.domain.type.TypeEffectivenessMatrix].
 *
 * [defensiveWeaknesses] maps every attacking type in the generation's [roster] to the number of
 * team members that take super-effective (≥2×) damage from it — a value of 0 means the team is
 * covered against that type. [offensiveGaps] lists the roster types that *no* team member can hit
 * super-effectively with one of its own types (STAB coverage holes).
 */
data class TeamCoverage(
  val roster: List<Type>,
  val defensiveWeaknesses: Map<Type, Int>,
  val offensiveGaps: List<Type>,
) {
  /** Distinct types at least one member is weak to. */
  val weakPointCount: Int get() = defensiveWeaknesses.count { it.value > 0 }
}
