package io.beanthemoonman.pokeapp.phone.ui.version

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.beanthemoonman.pokeapp.domain.model.Generation
import io.beanthemoonman.pokeapp.domain.usecase.GetGenerationsUseCase
import io.beanthemoonman.pokeapp.domain.usecase.ObserveSelectedGenerationUseCase
import io.beanthemoonman.pokeapp.domain.usecase.SelectGenerationUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class VersionSelectViewModel @Inject constructor(
  getGenerations: GetGenerationsUseCase,
  observeSelectedGeneration: ObserveSelectedGenerationUseCase,
  private val selectGeneration: SelectGenerationUseCase,
) : ViewModel() {

  val generations: List<Generation> = getGenerations()

  val selectedId: StateFlow<Int?> = observeSelectedGeneration()
    .map { it?.id }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  /** Persist the choice, then invoke [onSelected] (used to navigate into the shell). */
  fun select(id: Int, onSelected: () -> Unit) {
    viewModelScope.launch {
      Timber.i("user selected generation id=%d", id)
      selectGeneration(id)
      onSelected()
    }
  }
}
