package com.byteflipper.ffsensitivities.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(private val context: Context) {

    val selectedLanguage: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_LANGUAGE]
        }

    suspend fun saveLanguage(languageCode: String?) {
        context.dataStore.edit { settings ->
            if (languageCode != null) {
                settings[PreferencesKeys.SELECTED_LANGUAGE] = languageCode
            } else {
                settings.remove(PreferencesKeys.SELECTED_LANGUAGE)
            }
        }
    }

    private object PreferencesKeys {
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        // Add other keys like THEME, etc.
    }
}
