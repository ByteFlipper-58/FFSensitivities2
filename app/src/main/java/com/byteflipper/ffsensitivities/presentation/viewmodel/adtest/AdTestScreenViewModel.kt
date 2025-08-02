package com.byteflipper.ffsensitivities.presentation.viewmodel.adtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.presentation.adtest.events.AdTestScreenEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AdTestScreenViewModel @Inject constructor() : ViewModel() {

    private val _navigationEvent = MutableStateFlow<AdTestScreenEvent?>(null)
    val navigationEvent: StateFlow<AdTestScreenEvent?> = _navigationEvent

    fun handleEvent(event: AdTestScreenEvent) {
        when (event) {
            is AdTestScreenEvent.NavigateBack -> {
                _navigationEvent.value = event
            }
            is AdTestScreenEvent.ShowAppOpenAd -> {
                _navigationEvent.value = event
            }
            is AdTestScreenEvent.ShowInterstitialAd -> {
                _navigationEvent.value = event
            }
            is AdTestScreenEvent.ShowRewardedAd -> {
                _navigationEvent.value = event
            }
            is AdTestScreenEvent.TrackAction -> {
                _navigationEvent.value = event
            }
        }
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
} 