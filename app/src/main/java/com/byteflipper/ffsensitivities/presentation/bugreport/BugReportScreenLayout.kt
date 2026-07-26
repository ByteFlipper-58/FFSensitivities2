package com.byteflipper.ffsensitivities.presentation.bugreport

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.byteflipper.ffsensitivities.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.byteflipper.ffsensitivities.data.remote.SubmissionError
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

@Composable
fun BugReportScreenLayout(
    navController: NavController,
    viewModel: BugReportScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val categories = listOf(
        "support" to stringResource(id = R.string.bug_category_support),
        "bug_report" to stringResource(id = R.string.bug_category_report_bug),
        "settings_request" to stringResource(id = R.string.bug_category_request_settings),
        "settings_not_working" to stringResource(id = R.string.bug_category_settings_not_working),
        "feature_request" to stringResource(id = R.string.bug_category_feature_request),
        "other" to stringResource(id = R.string.bug_category_other)
    )
    val selectedCategoryLabel = categories.firstOrNull { it.first == uiState.selectedCategoryTag }?.second.orEmpty()
    val submissionErrorMessage = uiState.submissionError?.toMessage(context)

    LaunchedEffect(uiState.isSuccess, uiState.submissionError) {
        if (uiState.isSuccess) {
            delay(1500)
            viewModel.markSuccessHandled()
            navController.popBackStack()
        }
        if (uiState.submissionError != null) {
            delay(3000)
            viewModel.clearSubmissionError()
        }
    }

    BugReportScreenScaffold(
        navController = navController
    ) {
        BugReportScreenContent(
            selectedCategory = selectedCategoryLabel,
            description = uiState.description,
            expanded = uiState.expanded,
            includeLogs = uiState.includeLogs,
            isSubmitting = uiState.isSubmitting,
            isSuccess = uiState.isSuccess,
            submissionError = submissionErrorMessage,
            categories = categories.map { it.second },
            onCategorySelected = { selectedLabel ->
                categories
                    .firstOrNull { it.second == selectedLabel }
                    ?.let { (tag, _) -> viewModel.onCategorySelected(tag) }
            },
            onDescriptionChange = viewModel::onDescriptionChanged,
            onExpandedChange = viewModel::onExpandedChanged,
            onIncludeLogsChange = viewModel::onIncludeLogsChanged,
            onSubmit = viewModel::submitBugReport
        )
    }
}

private fun SubmissionError.toMessage(context: android.content.Context): String {
    val reason = when (this) {
        is SubmissionError.Validation -> reason
        is SubmissionError.Network -> context.getString(R.string.no_internet_connection)
        is SubmissionError.Server -> reason
        is SubmissionError.Unknown -> reason
    }

    return context.getString(
        R.string.bug_report_submission_error,
        reason.ifBlank { context.getString(R.string.unknown_error) }
    )
}
