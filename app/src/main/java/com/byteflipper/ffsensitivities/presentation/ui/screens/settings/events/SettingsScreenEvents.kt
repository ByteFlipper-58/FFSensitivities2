package com.byteflipper.ffsensitivities.presentation.ui.screens.settings.events

sealed class SettingsScreenEvent {
    object NavigateBack : SettingsScreenEvent()
    object NavigateToThemeSettings : SettingsScreenEvent()
    object NavigateToLanguageSettings : SettingsScreenEvent()
    object NavigateToPrivacySettings : SettingsScreenEvent()
    object NavigateToAdTest : SettingsScreenEvent()
} 