package com.byteflipper.ffsensitivities.presentation.viewmodel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.presentation.ui.screens.settings.events.SettingsScreenEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor() : ViewModel() {

    private val _navigationEvent = MutableStateFlow<SettingsScreenEvent?>(null)
    val navigationEvent: StateFlow<SettingsScreenEvent?> = _navigationEvent

    fun handleEvent(event: SettingsScreenEvent) {
        when (event) {
            is SettingsScreenEvent.NavigateBack -> {
                _navigationEvent.value = event
            }
            is SettingsScreenEvent.NavigateToThemeSettings -> {
                _navigationEvent.value = event
            }
            is SettingsScreenEvent.NavigateToLanguageSettings -> {
                _navigationEvent.value = event
            }
            is SettingsScreenEvent.NavigateToPrivacySettings -> {
                _navigationEvent.value = event
            }
            is SettingsScreenEvent.NavigateToAdTest -> {
                _navigationEvent.value = event
            }
        }
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
} 