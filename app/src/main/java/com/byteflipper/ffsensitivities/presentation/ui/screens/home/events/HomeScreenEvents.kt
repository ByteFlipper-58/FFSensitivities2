package com.byteflipper.ffsensitivities.presentation.ui.screens.home.events

sealed class HomeScreenEvent {
    object ShowRequestDialog : HomeScreenEvent()
    object HideRequestDialog : HomeScreenEvent()
    object SendSensitivitiesRequest : HomeScreenEvent()
    object NavigateToSettings : HomeScreenEvent()
    data class NavigateToDevices(val manufacturerName: String, val manufacturerModel: String) : HomeScreenEvent()
    object RetryLoadManufacturers : HomeScreenEvent()
    object TrackAdAction : HomeScreenEvent()
} 