package com.byteflipper.ffsensitivities.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

class DataStoreManager(private val context: Context) {
    private object PreferencesKeys {
        val THEME_KEY = stringPreferencesKey("app_theme")
        val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_colors")
        val CONTRAST_THEME_KEY = booleanPreferencesKey("contrast_theme")
        val VISIT_COUNT_KEY = intPreferencesKey("visit_count")
        val REQUEST_SENT_KEY = booleanPreferencesKey("request_sent")
        val FIRST_LAUNCH_COMPLETED_KEY = booleanPreferencesKey("first_launch_completed")
        val APP_LAUNCH_COUNT_KEY = intPreferencesKey("app_launch_count")
        val SELECTED_LANGUAGE_KEY = stringPreferencesKey("selected_language")
        
        // Счетчики рекламы для каждой локации
        val AD_COUNTER_HOME_SCREEN = intPreferencesKey("ad_counter_home_screen")
        val AD_COUNTER_DEVICES_SCREEN = intPreferencesKey("ad_counter_devices_screen")
        val AD_COUNTER_SENSITIVITIES_SCREEN = intPreferencesKey("ad_counter_sensitivities_screen")
        val AD_COUNTER_SETTINGS_SCREEN = intPreferencesKey("ad_counter_settings_screen")
    }

    suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { settings ->
            settings[key] = value
        }
    }

    fun <T> readNullable(key: Preferences.Key<T>): Flow<T?> = context.dataStore.data
        .map { settings ->
            settings[key]
        }

    fun <T> read(key: Preferences.Key<T>, defaultValue: T): Flow<T> = context.dataStore.data
        .map { settings ->
            settings[key] ?: defaultValue
        }

    suspend fun setTheme(theme: String) {
        save(PreferencesKeys.THEME_KEY, theme)
    }

    fun getTheme() = read(PreferencesKeys.THEME_KEY, "system")

    suspend fun setDynamicColor(dynamicColor: Boolean) {
        save(PreferencesKeys.DYNAMIC_COLOR_KEY, dynamicColor)
    }

    fun getDynamicColor() = read(PreferencesKeys.DYNAMIC_COLOR_KEY, false)

    suspend fun setContrastTheme(contrast: Boolean) {
        save(PreferencesKeys.CONTRAST_THEME_KEY, contrast)
    }

    fun getContrastTheme() = read(PreferencesKeys.CONTRAST_THEME_KEY, false)


    suspend fun setVisitCount(count: Int) {
        save(PreferencesKeys.VISIT_COUNT_KEY, count)
    }

    fun getVisitCount() = read(PreferencesKeys.VISIT_COUNT_KEY, 0)

    suspend fun setRequestSent(status: Boolean) {
        save(PreferencesKeys.REQUEST_SENT_KEY, status)
    }

    suspend fun setFirstLaunchCompleted(completed: Boolean) {
        save(PreferencesKeys.FIRST_LAUNCH_COMPLETED_KEY, completed)
    }

    fun getFirstLaunchCompleted(): Flow<Boolean> = read(PreferencesKeys.FIRST_LAUNCH_COMPLETED_KEY, false) // По умолчанию false (не завершен)

    fun getRequestSent() = read(PreferencesKeys.REQUEST_SENT_KEY, false)

    suspend fun incrementAppLaunchCount() {
        context.dataStore.edit { settings ->
            val currentCount = settings[PreferencesKeys.APP_LAUNCH_COUNT_KEY] ?: 0
            settings[PreferencesKeys.APP_LAUNCH_COUNT_KEY] = currentCount + 1
        }
    }

    fun getAppLaunchCount(): Flow<Int> = read(PreferencesKeys.APP_LAUNCH_COUNT_KEY, 0)

    // Методы для управления счетчиками рекламы
    suspend fun setLanguage(languageCode: String?) {
        context.dataStore.edit { settings ->
            if (languageCode != null) {
                settings[PreferencesKeys.SELECTED_LANGUAGE_KEY] = languageCode
            } else {
                settings.remove(PreferencesKeys.SELECTED_LANGUAGE_KEY)
            }
        }
    }

    fun getLanguage(): Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.SELECTED_LANGUAGE_KEY] }
    suspend fun setAdCounter(location: String, count: Int) {
        val key = when (location) {
            "HOME_SCREEN" -> PreferencesKeys.AD_COUNTER_HOME_SCREEN
            "DEVICES_SCREEN" -> PreferencesKeys.AD_COUNTER_DEVICES_SCREEN
            "SENSITIVITIES_SCREEN" -> PreferencesKeys.AD_COUNTER_SENSITIVITIES_SCREEN
            "SETTINGS_SCREEN" -> PreferencesKeys.AD_COUNTER_SETTINGS_SCREEN
            else -> return
        }
        save(key, count)
    }

    suspend fun getAdCounter(location: String): Int {
        val key = when (location) {
            "HOME_SCREEN" -> PreferencesKeys.AD_COUNTER_HOME_SCREEN
            "DEVICES_SCREEN" -> PreferencesKeys.AD_COUNTER_DEVICES_SCREEN
            "SENSITIVITIES_SCREEN" -> PreferencesKeys.AD_COUNTER_SENSITIVITIES_SCREEN
            "SETTINGS_SCREEN" -> PreferencesKeys.AD_COUNTER_SETTINGS_SCREEN
            else -> return 0
        }
        return context.dataStore.data.map { prefs -> prefs[key] ?: 0 }.first()
    }

    suspend fun incrementAdCounter(location: String): Int {
        val currentCount = getAdCounter(location)
        val newCount = currentCount + 1
        setAdCounter(location, newCount)
        return newCount
    }

    suspend fun resetAdCounter(location: String) {
        setAdCounter(location, 0)
    }
}
