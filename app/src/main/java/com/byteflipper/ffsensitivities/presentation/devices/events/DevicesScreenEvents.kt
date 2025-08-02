package com.byteflipper.ffsensitivities.presentation.devices.events

sealed class DevicesScreenEvent {
    object NavigateBack : DevicesScreenEvent()
    data class NavigateToSensitivities(val manufacturer: String, val modelName: String) : DevicesScreenEvent()
    object RetryLoadDevices : DevicesScreenEvent()
    object TrackAdAction : DevicesScreenEvent()
} 