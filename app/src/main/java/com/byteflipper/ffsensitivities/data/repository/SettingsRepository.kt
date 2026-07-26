package com.byteflipper.ffsensitivities.data.repository

import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStoreManager: DataStoreManager
) {
    fun getTheme(): Flow<String> = dataStoreManager.getTheme()

    suspend fun setTheme(theme: String) {
        dataStoreManager.setTheme(theme)
    }

    fun getDynamicColor(): Flow<Boolean> = dataStoreManager.getDynamicColor()

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStoreManager.setDynamicColor(enabled)
    }

    fun getContrastTheme(): Flow<Boolean> = dataStoreManager.getContrastTheme()

    suspend fun setContrastTheme(enabled: Boolean) {
        dataStoreManager.setContrastTheme(enabled)
    }

    fun getLanguage(): Flow<String?> = dataStoreManager.getLanguage()

    suspend fun setLanguage(languageCode: String?) {
        dataStoreManager.setLanguage(languageCode)
    }
}
