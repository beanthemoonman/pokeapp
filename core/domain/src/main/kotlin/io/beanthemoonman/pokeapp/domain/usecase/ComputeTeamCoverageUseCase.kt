package io.beanthemoonman.pokeapp.domain.usecase

import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.model.TeamCoverage
import io.beanthemoonman.pokeapp.domain.type.TypeEffectivenessMatrix
import javax.inject.Inject

/**
 * Computes a [TeamCoverage] for the (slotted) team in a generation. Pure, static computation over
 * [TypeEffectivenessMatrix] — never fetched. Null slots are ignored. An empty team yields a
 * roster-wide all-zero weakness map and every roster type as an offensive gap.
 */
class ComputeTeamCoverageUseCase @Inject constructor() {

    operator fun invoke(team: List<Pokemon?>, generation: Int): TeamCoverage {
        val roster = TypeEffectivenessMatrix.typesForGeneration(generation)
        val members = team.filterNotNull()

        // Defensive: per attacking type, how many members take ≥2× from it.
        val weaknesses = roster.associateWith { attacking ->
            members.count { member ->
                TypeEffectivenessMatrix.attackingEffectiveness(attacking, member.types, generation) >= SUPER_EFFECTIVE
            }
        }

        // Offensive: roster types that no member can hit ≥2× with one of its own (STAB) types.
        val gaps = roster.filter { defending ->
            members.none { member ->
                member.types.any { atk ->
                    TypeEffectivenessMatrix.effectiveness(atk, defending, generation) >= SUPER_EFFECTIVE
                }
            }
        }

        return TeamCoverage(roster = roster, defensiveWeaknesses = weaknesses, offensiveGaps = gaps)
    }

    private companion object {
        const val SUPER_EFFECTIVE = 2f
    }
}
