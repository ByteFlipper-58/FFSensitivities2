package com.byteflipper.ffsensitivities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.data.repository.SettingsRepository
import com.byteflipper.ffsensitivities.manager.AppLocaleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val appLocaleManager = AppLocaleManager()

    // StateFlow for the current language code
    private val _currentLanguageCode = MutableStateFlow(appLocaleManager.getCurrentLanguageCode())
    val currentLanguageCode: StateFlow<String> = _currentLanguageCode.asStateFlow()

    val theme: StateFlow<String> = settingsRepository.getTheme()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "system"
        )

    val dynamicColor: StateFlow<Boolean> = settingsRepository.getDynamicColor()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val contrastTheme: StateFlow<Boolean> = settingsRepository.getContrastTheme()
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
                settingsRepository.getTheme(),
                settingsRepository.getDynamicColor(),
                settingsRepository.getContrastTheme()
            ) { _, _, _ ->
                true
            }.first { it }

            _settingsLoaded.value = true
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setTheme(theme)
        }
    }

    fun setDynamicColor(dynamicColor: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setDynamicColor(dynamicColor)
        }
    }

    fun setContrastTheme(contrast: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setContrastTheme(contrast)
        }
    }

    fun setLanguage(languageCode: String?) {
        appLocaleManager.changeLanguage(languageCode)
        _currentLanguageCode.value = languageCode ?: "system"
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setLanguage(languageCode)
        }
    }
}
