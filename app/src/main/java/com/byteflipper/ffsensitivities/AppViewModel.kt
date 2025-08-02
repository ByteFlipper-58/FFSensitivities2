package com.byteflipper.ffsensitivities

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.ads.AdManager
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import com.byteflipper.ffsensitivities.manager.AppLocaleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class AppViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val dataStoreManager: DataStoreManager,
    val adManager: AdManager
) : AndroidViewModel(context.applicationContext as Application) {

    private val appLocaleManager = AppLocaleManager()

    // StateFlow for the current language code
    private val _currentLanguageCode = MutableStateFlow(appLocaleManager.getCurrentLanguageCode())
    val currentLanguageCode: StateFlow<String> = _currentLanguageCode.asStateFlow()

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

    val requestSent: StateFlow<Boolean> = dataStoreManager.getRequestSent()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    private val _settingsLoaded = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _settingsLoaded.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dataStoreManager.getTheme(),
                dataStoreManager.getDynamicColor(),
                dataStoreManager.getContrastTheme()
            ) { _, _, _ ->
                true
            }.first { it }

            _settingsLoaded.value = true
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

    fun setRequestSent(status: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.setRequestSent(status)
        }
    }

    fun setLanguage(languageCode: String?) {
        appLocaleManager.changeLanguage(context, languageCode)
        _currentLanguageCode.value = languageCode ?: "system"
    }
}
