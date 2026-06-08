package io.beanthemoonman.pokeapp.domain.usecase

import io.beanthemoonman.pokeapp.domain.model.Generation
import io.beanthemoonman.pokeapp.domain.model.Generations
import io.beanthemoonman.pokeapp.domain.repository.GenerationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** The full catalog of selectable generations. */
class GetGenerationsUseCase @Inject constructor() {
    operator fun invoke(): List<Generation> = Generations.all
}

/**
 * Observes the selected generation, resolved to its [Generation]. Emits null when
 * none is chosen yet (first launch → show the selector).
 */
class ObserveSelectedGenerationUseCase @Inject constructor(
    private val repository: GenerationRepository
) {
    operator fun invoke(): Flow<Generation?> =
        repository.selectedGenerationId.map { id -> id?.let { Generations.byId(it) } }
}

/** Persists the user's generation choice. */
class SelectGenerationUseCase @Inject constructor(
    private val repository: GenerationRepository
) {
    suspend operator fun invoke(id: Int) = repository.selectGeneration(id)
}
