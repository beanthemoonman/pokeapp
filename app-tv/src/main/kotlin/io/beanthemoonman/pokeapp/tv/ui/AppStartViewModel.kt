package io.beanthemoonman.pokeapp.tv.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.beanthemoonman.pokeapp.domain.usecase.ObserveSelectedGenerationUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

/** Where the app should open: the selector (no choice yet) or the main shell. */
enum class StartState { Loading, Selector, Shell }

@HiltViewModel
class AppStartViewModel @Inject constructor(
  observeSelectedGeneration: ObserveSelectedGenerationUseCase,
) : ViewModel() {

  val startState: StateFlow<StartState> = observeSelectedGeneration()
    .map { if (it == null) StartState.Selector else StartState.Shell }
    .onEach { Timber.i("tv app start gate -> %s", it) }
    .stateIn(viewModelScope, SharingStarted.Eagerly, StartState.Loading)
}
