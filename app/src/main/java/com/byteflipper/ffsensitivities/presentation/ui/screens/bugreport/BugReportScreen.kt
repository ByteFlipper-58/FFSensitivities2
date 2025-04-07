package com.byteflipper.ffsensitivities.presentation.ui.screens.bugreport

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.data.remote.sendBugReport
import com.byteflipper.ffsensitivities.presentation.ui.screens.bugreport.components.BugReportHeader
import com.byteflipper.ffsensitivities.presentation.ui.screens.bugreport.components.CategorySelector
import com.byteflipper.ffsensitivities.presentation.ui.screens.bugreport.components.DescriptionInput
import com.byteflipper.ffsensitivities.presentation.ui.screens.bugreport.components.InfoNote
import com.byteflipper.ffsensitivities.presentation.ui.screens.bugreport.components.LogsOption
import com.byteflipper.ffsensitivities.presentation.ui.screens.bugreport.components.SubmissionStatusOverlay
import com.byteflipper.ffsensitivities.presentation.ui.screens.bugreport.components.SubmitButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReportScreen(navController: NavController) {
    var selectedCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var includeLogs by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var submissionError by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val categories = listOf(
        stringResource(id = R.string.bug_category_support),
        stringResource(id = R.string.bug_category_report_bug),
        stringResource(id = R.string.bug_category_visual_error),
        stringResource(id = R.string.bug_category_request_settings),
        stringResource(id = R.string.bug_category_settings_not_working),
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.report_bug_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back_ios_24px),
                            contentDescription = stringResource(id = R.string.bug_report_back_button_desc),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BugReportHeader()

                CategorySelector(
                    selectedCategory = selectedCategory,
                    categories = categories,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    onCategorySelected = { selectedCategory = it }
                )

                DescriptionInput(
                    description = description,
                    onDescriptionChange = { description = it }
                )

                LogsOption(
                    includeLogs = includeLogs,
                    onIncludeLogsChange = { includeLogs = it }
                )

                InfoNote()

                Spacer(modifier = Modifier.height(8.dp))

                SubmitButton(
                    enabled = selectedCategory.isNotEmpty() && description.isNotEmpty() && !isSubmitting,
                    onClick = {
                        scope.launch {
                            isSubmitting = true
                            submissionError = null
                            isSuccess = false

                            val selectedIndex = categories.indexOf(selectedCategory)
                            val tag = if (selectedIndex != -1 && selectedIndex < categories_tags.size) {
                                categories_tags[selectedIndex]
                            } else {
                                Timber.w("Selected category '$selectedCategory' not found in list or tags list mismatch. Defaulting to 'other'.")
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

                            val result = sendBugReport(tag, formattedMessage)

                            result.onSuccess {
                                Timber.i("Bug report submitted successfully. Ticket: $ticketId")
                                isSuccess = true
                            }.onFailure { error ->
                                Timber.e(error, "Bug report submission failed.")
                                submissionError = context.getString(R.string.bug_report_submission_error, error.message ?: context.getString(R.string.unknown_error))
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            SubmissionStatusOverlay(
                isSubmitting = isSubmitting,
                isSuccess = isSuccess,
                submissionError = submissionError
            )
        }
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
