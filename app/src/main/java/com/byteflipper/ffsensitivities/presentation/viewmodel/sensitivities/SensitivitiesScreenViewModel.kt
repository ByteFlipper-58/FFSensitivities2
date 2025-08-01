package com.byteflipper.ffsensitivities.presentation.viewmodel.sensitivities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.presentation.ui.screens.sensitivities.events.SensitivitiesScreenEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SensitivitiesScreenViewModel @Inject constructor() : ViewModel() {

    private val _navigationEvent = MutableStateFlow<SensitivitiesScreenEvent?>(null)
    val navigationEvent: StateFlow<SensitivitiesScreenEvent?> = _navigationEvent

    fun handleEvent(event: SensitivitiesScreenEvent) {
        when (event) {
            is SensitivitiesScreenEvent.NavigateBack -> {
                _navigationEvent.value = event
            }
            is SensitivitiesScreenEvent.CopySettings -> {
                _navigationEvent.value = event
            }
            is SensitivitiesScreenEvent.ThumbUp -> {
                _navigationEvent.value = event
            }
            is SensitivitiesScreenEvent.ThumbDown -> {
                _navigationEvent.value = event
            }
            is SensitivitiesScreenEvent.RetryLoadDevice -> {
                _navigationEvent.value = event
            }
        }
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
} 