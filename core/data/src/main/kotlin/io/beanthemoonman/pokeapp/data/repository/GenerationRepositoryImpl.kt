package io.beanthemoonman.pokeapp.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.beanthemoonman.pokeapp.domain.repository.GenerationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "pokedex_settings")

@Singleton
class GenerationRepositoryImpl @Inject constructor(
  @param:ApplicationContext private val context: Context
) : GenerationRepository {

  override val selectedGenerationId: Flow<Int?> =
    context.settingsDataStore.data
      .map { prefs -> prefs[KEY_SELECTED_GENERATION] }
      .onEach { Timber.d("selectedGenerationId emitted -> %s", it) }

  override suspend fun selectGeneration(id: Int) {
    Timber.i("selectGeneration persisting id=%d", id)
    context.settingsDataStore.edit { prefs -> prefs[KEY_SELECTED_GENERATION] = id }
  }

  private companion object {
    val KEY_SELECTED_GENERATION = intPreferencesKey("selected_generation")
  }
}
