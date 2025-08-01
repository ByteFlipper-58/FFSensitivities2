package com.byteflipper.ffsensitivities.presentation.ui.screens.adtest.events

sealed class AdTestScreenEvent {
    object NavigateBack : AdTestScreenEvent()
    object ShowAppOpenAd : AdTestScreenEvent()
    data class ShowInterstitialAd(val location: String) : AdTestScreenEvent()
    object ShowRewardedAd : AdTestScreenEvent()
    data class TrackAction(val location: String) : AdTestScreenEvent()
} 