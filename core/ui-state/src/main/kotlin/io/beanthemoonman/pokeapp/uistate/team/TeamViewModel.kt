package io.beanthemoonman.pokeapp.uistate.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.beanthemoonman.pokeapp.domain.model.Generations
import io.beanthemoonman.pokeapp.domain.model.Pokemon
import io.beanthemoonman.pokeapp.domain.model.UiState
import io.beanthemoonman.pokeapp.domain.usecase.ComputeTeamCoverageUseCase
import io.beanthemoonman.pokeapp.domain.usecase.ObserveSelectedGenerationUseCase
import io.beanthemoonman.pokeapp.domain.usecase.ObserveTeamUseCase
import io.beanthemoonman.pokeapp.domain.usecase.ObserveTeamUseCase.Companion.TEAM_SIZE
import io.beanthemoonman.pokeapp.domain.usecase.SearchPokemonUseCase
import io.beanthemoonman.pokeapp.domain.usecase.SetTeamSlotUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class TeamViewModel @Inject constructor(
  observeSelectedGeneration: ObserveSelectedGenerationUseCase,
  observeTeam: ObserveTeamUseCase,
  private val computeCoverage: ComputeTeamCoverageUseCase,
  private val searchPokemon: SearchPokemonUseCase,
  private val setTeamSlot: SetTeamSlotUseCase,
) : ViewModel() {

  /** Active generation (drives dex bounds for the picker + which type chart the coverage uses). */
  private val generation = observeSelectedGeneration()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  /** The six saved slots; the repository always emits a [TEAM_SIZE]-long list. */
  private val team: StateFlow<List<Pokemon?>> = observeTeam()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), List(TEAM_SIZE) { null })

  val state: StateFlow<UiState<TeamData>> =
    combine(generation, team) { gen, slots ->
      val g = gen ?: Generations.latest
      Timber.d("team gen=%d filled=%d", g.id, slots.count { it != null })
      UiState.Success(
        TeamData(
          generation = g,
          team = slots,
          coverage = computeCoverage(slots, g.id),
        )
      ) as UiState<TeamData>
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

  // ── Picker overlay ──────────────────────────────────────────────────────────
  /** Slot the picker is currently targeting; null = closed. */
  private val pickerSlot = MutableStateFlow<Int?>(null)
  private val pickerQuery = MutableStateFlow("")

  /**
   * Picker search results, recomputed on the debounced query / generation. Only runs while the
   * picker is open with a non-blank query; otherwise [PickerResults.Idle]. `flatMapLatest`
   * cancels an in-flight search as the user keeps typing.
   */
  private val pickerResults: StateFlow<PickerResults> =
    combine(
      pickerSlot,
      pickerQuery.debounce { if (it.isBlank()) 0L else SEARCH_DEBOUNCE_MS }.map { it.trim() },
      generation.filterNotNull(),
    ) { slot, q, gen -> Triple(slot, q, gen) }
      .distinctUntilChanged()
      .flatMapLatest { (slot, q, gen) ->
        if (slot == null || q.isEmpty()) {
          flowOf(PickerResults.Idle)
        } else {
          flow {
            Timber.i("team picker search q=%s gen=%d dexEnd=%d", q, gen.id, gen.dexEnd)
            emit(PickerResults.Loading)
            val results = searchPokemon(q, gen.dexEnd)
            Timber.d("team picker search q=%s results=%d", q, results.size)
            emit(if (results.isEmpty()) PickerResults.Empty else PickerResults.Results(results))
          }.catch { e ->
            Timber.e(e, "team picker search failed q=%s", q)
            emit(PickerResults.Error)
          }
        }
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PickerResults.Idle)

  val picker: StateFlow<TeamPickerUiState> =
    combine(pickerSlot, pickerQuery, pickerResults, team) { slot, q, results, slots ->
      slot?.let {
        TeamPickerUiState.Open(
          slot = it,
          replacing = slots.getOrNull(it) != null,
          query = q,
          results = results
        )
      } ?: TeamPickerUiState.Closed
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TeamPickerUiState.Closed)

  /** Open the picker on a specific slot (tapping a slot tile). */
  fun openPicker(slot: Int) {
    require(slot in 0 until TEAM_SIZE) { "Team slot must be in 0..${TEAM_SIZE - 1}" }
    Timber.i("team picker opened slot=%d replacing=%b", slot, team.value.getOrNull(slot) != null)
    pickerQuery.value = ""
    pickerSlot.value = slot
  }

  fun closePicker() {
    Timber.d("team picker closed")
    pickerSlot.value = null
    pickerQuery.value = ""
  }

  fun onPickerQueryChange(value: String) {
    pickerQuery.value = value
  }

  /** Assign the picked Pokémon to the open slot, then close the overlay. */
  fun selectPokemon(pokemon: Pokemon) {
    val slot = pickerSlot.value ?: run {
      Timber.w("selectPokemon ignored: picker is closed")
      return
    }
    Timber.i("team slot=%d set -> id=%d (%s)", slot, pokemon.id, pokemon.name)
    viewModelScope.launch { setTeamSlot(slot, pokemon.id) }
    closePicker()
  }

  /** Clear a slot. Closes the picker if it was open on that slot. */
  fun removeSlot(slot: Int) {
    Timber.i("team slot=%d cleared", slot)
    viewModelScope.launch { setTeamSlot(slot, null) }
    if (pickerSlot.value == slot) closePicker()
  }

  private companion object {
    const val SEARCH_DEBOUNCE_MS = 300L
  }
}
