package com.byteflipper.ffsensitivities.presentation.bugreport

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.data.repository.BugReportLogRepository
import com.byteflipper.ffsensitivities.data.remote.BugReportApiService
import com.byteflipper.ffsensitivities.data.remote.SubmissionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BugReportScreenViewModel @Inject constructor(
    private val bugReportApiService: BugReportApiService,
    private val bugReportLogRepository: BugReportLogRepository
) : ViewModel() {

    companion object {
        private const val TAG = "BugReportScreen"
    }

    private val _uiState = MutableStateFlow(BugReportUiState())
    val uiState: StateFlow<BugReportUiState> = _uiState.asStateFlow()

    fun onCategorySelected(categoryTag: String) {
        _uiState.value = _uiState.value.copy(selectedCategoryTag = categoryTag)
    }

    fun onDescriptionChanged(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun onExpandedChanged(expanded: Boolean) {
        _uiState.value = _uiState.value.copy(expanded = expanded)
    }

    fun onIncludeLogsChanged(includeLogs: Boolean) {
        _uiState.value = _uiState.value.copy(includeLogs = includeLogs)
    }

    fun clearSubmissionError() {
        _uiState.value = _uiState.value.copy(submissionError = null, isSubmitting = false)
    }

    fun markSuccessHandled() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }

    fun submitBugReport() {
        val currentState = _uiState.value
        if (currentState.selectedCategoryTag.isBlank() || currentState.description.isBlank()) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isSubmitting = true,
                isSuccess = false,
                submissionError = null
            )

            val formattedMessage = buildMessage(
                selectedCategoryTag = currentState.selectedCategoryTag,
                description = currentState.description,
                includeLogs = currentState.includeLogs
            )

            when (val result = bugReportApiService.submitBugReport(currentState.selectedCategoryTag, formattedMessage)) {
                is SubmissionResult.Success -> {
                    Log.i(TAG, "Bug report submitted successfully")
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        isSuccess = true
                    )
                }

                is SubmissionResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        submissionError = result.error
                    )
                }
            }
        }
    }

    private fun buildMessage(
        selectedCategoryTag: String,
        description: String,
        includeLogs: Boolean
    ): String {
        val ticketId = buildTicketId()
        var baseMessage = description

        if (includeLogs) {
            val logContent = bugReportLogRepository.readLogFileContent()
            baseMessage += "\n\n--- Logs ---\n$logContent"
        }

        return """
            |#$selectedCategoryTag
            |
            |Description:
            |$baseMessage
            |
            |Ticket:
            |$ticketId
        """.trimMargin()
    }

    private fun buildTicketId(): String {
        val dateFormat = SimpleDateFormat("ddMMyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val randomDigits = (100..999).random()
        val randomChars = (1..3).map { ('a'..'z').random() }.joinToString("")
        return "$currentDate:$randomDigits$randomChars"
    }
}

data class BugReportUiState(
    val selectedCategoryTag: String = "",
    val description: String = "",
    val expanded: Boolean = false,
    val includeLogs: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val submissionError: com.byteflipper.ffsensitivities.data.remote.SubmissionError? = null
)
