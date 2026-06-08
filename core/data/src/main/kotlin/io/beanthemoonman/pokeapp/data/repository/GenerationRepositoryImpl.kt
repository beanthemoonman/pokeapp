package io.beanthemoonman.pokeapp.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.beanthemoonman.pokeapp.domain.repository.GenerationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "pokedex_settings")

@Singleton
class GenerationRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : GenerationRepository {

    override val selectedGenerationId: Flow<Int?> =
        context.settingsDataStore.data.map { prefs -> prefs[KEY_SELECTED_GENERATION] }

    override suspend fun selectGeneration(id: Int) {
        context.settingsDataStore.edit { prefs -> prefs[KEY_SELECTED_GENERATION] = id }
    }

    private companion object {
        val KEY_SELECTED_GENERATION = intPreferencesKey("selected_generation")
    }
}
