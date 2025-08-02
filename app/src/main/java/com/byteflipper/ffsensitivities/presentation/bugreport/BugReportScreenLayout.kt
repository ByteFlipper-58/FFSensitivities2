package com.byteflipper.ffsensitivities.presentation.bugreport

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.data.remote.BugReportApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BugReportScreenLayout(
    navController: NavController
) {
    val TAG = "BugReportScreen" // Define TAG for logging
    var selectedCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var includeLogs by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var submissionError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val categories = listOf(
        stringResource(id = R.string.bug_category_support),
        stringResource(id = R.string.bug_category_report_bug),
        stringResource(id = R.string.bug_category_request_settings),
        stringResource(id = R.string.bug_category_settings_not_working),
        stringResource(id = R.string.bug_category_feature_request),
        stringResource(id = R.string.bug_category_other)
    )

    val categories_tags = listOf(
        "support",
        "bug_report",
        "settings_request",
        "settings_not_working",
        "feature_request",
        "other"
    )

    LaunchedEffect(isSuccess, submissionError) {
        if (isSuccess) {
            delay(1500)
            navController.popBackStack()
        }
        if (submissionError != null) {
            delay(3000)
             isSubmitting = false
        }
    }

    BugReportScreenScaffold(
        navController = navController
    ) {
        BugReportScreenContent(
            selectedCategory = selectedCategory,
            description = description,
            expanded = expanded,
            includeLogs = includeLogs,
            isSubmitting = isSubmitting,
            isSuccess = isSuccess,
            submissionError = submissionError,
            categories = categories,
            categories_tags = categories_tags,
            onCategorySelected = { selectedCategory = it },
            onDescriptionChange = { description = it },
            onExpandedChange = { expanded = it },
            onIncludeLogsChange = { includeLogs = it },
            onSubmit = {
                scope.launch {
                    isSubmitting = true
                    submissionError = null
                    isSuccess = false

                    val selectedIndex = categories.indexOf(selectedCategory)
                    val tag = if (selectedIndex != -1 && selectedIndex < categories_tags.size) {
                        categories_tags[selectedIndex]
                    } else {
                        android.util.Log.w(TAG, "Selected category '$selectedCategory' not found in list or tags list mismatch. Defaulting to 'other'.")
                        "other"
                    }

                    val dateFormat = SimpleDateFormat("ddMMyy", Locale.getDefault())
                    val currentDate = dateFormat.format(Date())
                    val randomDigits = (100..999).random()
                    val randomChars = (1..3).map { ('a'..'z').random() }.joinToString("")
                    val ticketId = "$currentDate:$randomDigits$randomChars"

                    var baseMessage = description
                    if (includeLogs) {
                        val logContent = readLogFileContent(context)
                        baseMessage += "\n\n--- Logs ---\n$logContent"
                    }

                    val formattedMessage = """
                        |#$tag
                        |
                        |Description:
                        |$baseMessage
                        |
                        |Ticket:
                        |$ticketId
                    """.trimMargin()

                    val result = BugReportApiService.submitBugReport(tag, formattedMessage)

                    result.onSuccess {
                        android.util.Log.i(TAG, "Bug report submitted successfully. Ticket: $ticketId")
                        isSuccess = true
                    }.onFailure { error ->
                        android.util.Log.e(TAG, "Bug report submission failed.", error)
                        submissionError = context.getString(R.string.bug_report_submission_error, error.message ?: context.getString(R.string.unknown_error))
                    }
                }
            }
        )
    }
}

private fun readLogFileContent(context: Context): String {
    val logDir = File(context.getExternalFilesDir(null), "logs")
    val logFile = File(logDir, "logs.txt")
    return try {
        if (logFile.exists()) {
            logFile.readText()
        } else {
            context.getString(R.string.bug_report_log_not_found)
        }
    } catch (e: IOException) {
        context.getString(R.string.bug_report_log_read_error, e.message ?: context.getString(R.string.unknown_error))
    }
} 