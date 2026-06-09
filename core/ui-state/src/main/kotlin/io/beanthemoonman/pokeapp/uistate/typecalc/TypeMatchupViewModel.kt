package io.beanthemoonman.pokeapp.uistate.typecalc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.beanthemoonman.pokeapp.domain.model.Generation
import io.beanthemoonman.pokeapp.domain.model.Generations
import io.beanthemoonman.pokeapp.domain.model.Type
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.type.DefenseBucket
import io.beanthemoonman.pokeapp.domain.type.TypeEffectivenessMatrix
import io.beanthemoonman.pokeapp.domain.usecase.GroupDefenseEffectivenessUseCase
import io.beanthemoonman.pokeapp.domain.usecase.ObserveSelectedGenerationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

/**
 * State for the Type Matchup screen: a defensive calculator. The user picks 1–2 defending types
 * (a valid mono/dual combo) and [defenseGroups] shows how every attacking type in the generation
 * hits that combo, bucketed ×4 … ×0. No combo picked yet → [defending] is false (prompt state).
 */
data class TypeMatchupData(
    val generation: Generation,
    val roster: List<Type>,
    val defenders: List<Type>,
    val defenseGroups: Map<DefenseBucket, List<Type>>,
) {
    val defending: Boolean get() = defenders.isNotEmpty()
}

@HiltViewModel
class TypeMatchupViewModel @Inject constructor(
    observeSelectedGeneration: ObserveSelectedGenerationUseCase,
    private val groupDefense: GroupDefenseEffectivenessUseCase,
) : ViewModel() {

    /** Up to [MAX_DEFENDERS] distinct defending types; empty = nothing picked yet. */
    private val defenderSelection = MutableStateFlow<List<Type>>(emptyList())

    val state: StateFlow<UiState<TypeMatchupData>> =
        combine(
            observeSelectedGeneration(),
            defenderSelection,
        ) { generation, defenderPicks ->
            val gen = generation ?: Generations.latest
            val roster = TypeEffectivenessMatrix.typesForGeneration(gen.id)
            // Drop anything the active generation's roster doesn't include (e.g. Fairy in Gen I).
            val defenders = defenderPicks.filter { it in roster }

            Timber.d("matchup gen=%d defenders=%s", gen.id, defenders)
            UiState.Success(
                TypeMatchupData(
                    generation = gen,
                    roster = roster,
                    defenders = defenders,
                    defenseGroups = groupDefense(defenders, gen.id),
                )
            ) as UiState<TypeMatchupData>
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    /**
     * Toggles a defending type. Removing if present; otherwise adding, capped at
     * [MAX_DEFENDERS] by evicting the oldest pick so the newest tap always lands.
     */
    fun toggleDefender(type: Type) {
        val current = defenderSelection.value
        val next = when {
            type in current -> current - type
            current.size < MAX_DEFENDERS -> current + type
            else -> current.drop(1) + type
        }
        Timber.i("matchup defenders %s -> %s", current, next)
        defenderSelection.value = next
    }

    fun clearDefenders() {
        Timber.i("matchup defenders cleared")
        defenderSelection.value = emptyList()
    }

    private companion object {
        const val MAX_DEFENDERS = 2 // a valid Pokémon type combo is mono- or dual-type
    }
}
