package com.byteflipper.ffsensitivities

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AppViewModel @Inject constructor(
    application: Application,
    private val dataStoreManager: DataStoreManager
) : AndroidViewModel(application) {

    val theme: StateFlow<String> = dataStoreManager.getTheme()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "system"
        )

    val dynamicColor: StateFlow<Boolean> = dataStoreManager.getDynamicColor()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
        initialValue = false
    )

    val contrastTheme: StateFlow<Boolean> = dataStoreManager.getContrastTheme()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val visitCount: StateFlow<Int> = dataStoreManager.getVisitCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 0
        )

    val requestSent: StateFlow<Boolean> = dataStoreManager.getRequestSent()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )


    private val _settingsLoaded = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _settingsLoaded.asStateFlow()

    init {
        // Coroutine to wait for actual settings load
        viewModelScope.launch {
            // Combine the flows from DataStoreManager directly
            // Combine flows for theme and dynamic color only
            combine(
                dataStoreManager.getTheme(),
                dataStoreManager.getDynamicColor(),
                dataStoreManager.getContrastTheme()
                // Removed language flow
            ) { theme, dynamic, contrast ->
                // This lambda executes when all flows have emitted at least one value.
                true // Indicate that loading is complete
            }.first { it } // Collect the first emission where the value is true

            // Mark settings as loaded
            _settingsLoaded.value = true
            // No need to apply language here, system handles it
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.setTheme(theme)
        }
    }

    fun setDynamicColor(dynamicColor: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.setDynamicColor(dynamicColor)
        }
    }

    fun setContrastTheme(contrast: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.setContrastTheme(contrast)
        }
    }

    fun setVisitCount(count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.setVisitCount(count)
        }
    }

    fun setRequestSent(status: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.setRequestSent(status)
        }
    }

    // Updated setLanguage: only applies the locale, doesn't save to DataStore
    fun setLanguage(languageCode: String?) {
        val localesToSet = if (languageCode != null) {
            LocaleListCompat.forLanguageTags(languageCode)
        } else {
            LocaleListCompat.getEmptyLocaleList() // System default
        }
        // Apply the change immediately on the main thread
        // No need for viewModelScope or Dispatchers.IO as it's a UI-related call
        AppCompatDelegate.setApplicationLocales(localesToSet)
    }
}
