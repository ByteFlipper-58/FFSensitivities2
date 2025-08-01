package com.byteflipper.ffsensitivities.presentation.viewmodel.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.presentation.ui.screens.about.events.AboutScreenEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AboutScreenViewModel @Inject constructor() : ViewModel() {

    private val _navigationEvent = MutableStateFlow<AboutScreenEvent?>(null)
    val navigationEvent: StateFlow<AboutScreenEvent?> = _navigationEvent

    fun handleEvent(event: AboutScreenEvent) {
        when (event) {
            is AboutScreenEvent.NavigateBack -> {
                _navigationEvent.value = event
            }
            is AboutScreenEvent.NavigateToBugReport -> {
                _navigationEvent.value = event
            }
            is AboutScreenEvent.LaunchReviewFlow -> {
                _navigationEvent.value = event
            }
            is AboutScreenEvent.OpenPlayStore -> {
                _navigationEvent.value = event
            }
            is AboutScreenEvent.OpenWebsite -> {
                _navigationEvent.value = event
            }
            is AboutScreenEvent.OpenVK -> {
                _navigationEvent.value = event
            }
            is AboutScreenEvent.OpenTelegram -> {
                _navigationEvent.value = event
            }
            is AboutScreenEvent.OpenGitHub -> {
                _navigationEvent.value = event
            }
            is AboutScreenEvent.OpenSourceCode -> {
                _navigationEvent.value = event
            }
        }
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
} 