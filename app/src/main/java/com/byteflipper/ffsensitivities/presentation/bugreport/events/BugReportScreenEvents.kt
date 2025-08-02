package com.byteflipper.ffsensitivities.presentation.bugreport.events

sealed class BugReportScreenEvent {
    object NavigateBack : BugReportScreenEvent()
    data class SelectCategory(val category: String) : BugReportScreenEvent()
    data class UpdateDescription(val description: String) : BugReportScreenEvent()
    data class ToggleExpanded(val expanded: Boolean) : BugReportScreenEvent()
    data class ToggleIncludeLogs(val includeLogs: Boolean) : BugReportScreenEvent()
    object SubmitBugReport : BugReportScreenEvent()
    object ClearSubmissionError : BugReportScreenEvent()
} 