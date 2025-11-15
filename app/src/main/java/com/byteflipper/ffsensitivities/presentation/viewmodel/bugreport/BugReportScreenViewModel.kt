package com.byteflipper.ffsensitivities.presentation.viewmodel.bugreport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.data.remote.BugReportApiService
import com.byteflipper.ffsensitivities.data.remote.SubmissionResult
import com.byteflipper.ffsensitivities.presentation.bugreport.events.BugReportScreenEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BugReportScreenViewModel @Inject constructor(
    private val bugReportApiService: BugReportApiService
) : ViewModel() {

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

    suspend fun submitBugReport(tag: String, message: String): SubmissionResult {
        return bugReportApiService.submitBugReport(tag, message)
    }
} 
