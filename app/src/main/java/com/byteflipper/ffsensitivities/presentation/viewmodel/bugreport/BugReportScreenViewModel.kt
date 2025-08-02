package com.byteflipper.ffsensitivities.presentation.viewmodel.bugreport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.presentation.bugreport.events.BugReportScreenEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class BugReportScreenViewModel @Inject constructor() : ViewModel() {

    private val _navigationEvent = MutableStateFlow<BugReportScreenEvent?>(null)
    val navigationEvent: StateFlow<BugReportScreenEvent?> = _navigationEvent

    fun handleEvent(event: BugReportScreenEvent) {
        when (event) {
            is BugReportScreenEvent.NavigateBack -> {
                _navigationEvent.value = event
            }
            is BugReportScreenEvent.SelectCategory -> {
                _navigationEvent.value = event
            }
            is BugReportScreenEvent.UpdateDescription -> {
                _navigationEvent.value = event
            }
            is BugReportScreenEvent.ToggleExpanded -> {
                _navigationEvent.value = event
            }
            is BugReportScreenEvent.ToggleIncludeLogs -> {
                _navigationEvent.value = event
            }
            is BugReportScreenEvent.SubmitBugReport -> {
                _navigationEvent.value = event
            }
            is BugReportScreenEvent.ClearSubmissionError -> {
                _navigationEvent.value = event
            }
        }
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
} 