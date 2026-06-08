package io.beanthemoonman.pokeapp.domain.usecase

import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.domain.type.DefenseBucket
import io.beanthemoonman.pokeapp.domain.type.TypeEffectivenessMatrix
import javax.inject.Inject

/**
 * Groups every attacking type in a generation by how hard it hits a [defenders] type combo
 * (mono- or dual-type), bucketing the stacked incoming multiplier (×4 … ×0). Pure, static
 * computation over [TypeEffectivenessMatrix] — never fetched.
 *
 * Returns all six [DefenseBucket] keys (empty lists when a bucket has no members) in display
 * order, each list in roster order. An empty [defenders] list yields all-empty buckets.
 */
class GroupDefenseEffectivenessUseCase @Inject constructor() {

    operator fun invoke(defenders: List<Type>, generation: Int): Map<DefenseBucket, List<Type>> {
        if (defenders.isEmpty()) {
            return DefenseBucket.entries.associateWith { emptyList() }
        }
        val incoming = TypeEffectivenessMatrix.defendingWeaknesses(defenders, generation)
        val byBucket = incoming.entries.groupBy(
            keySelector = { DefenseBucket.forMultiplier(it.value) },
            valueTransform = { it.key },
        )
        return DefenseBucket.entries.associateWith { byBucket[it].orEmpty() }
    }
}
