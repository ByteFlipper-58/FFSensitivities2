package com.byteflipper.ffsensitivities.presentation.bugreport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byteflipper.ffsensitivities.presentation.ui.screens.bugreport.components.BugReportHeader
import com.byteflipper.ffsensitivities.presentation.ui.screens.bugreport.components.CategorySelector
import com.byteflipper.ffsensitivities.presentation.ui.screens.bugreport.components.DescriptionInput
import com.byteflipper.ffsensitivities.presentation.ui.screens.bugreport.components.InfoNote
import com.byteflipper.ffsensitivities.presentation.ui.screens.bugreport.components.SubmissionStatusOverlay
import com.byteflipper.ffsensitivities.presentation.ui.screens.bugreport.components.SubmitButton

@Composable
fun BugReportScreenContent(
    selectedCategory: String,
    description: String,
    expanded: Boolean,
    includeLogs: Boolean,
    isSubmitting: Boolean,
    isSuccess: Boolean,
    submissionError: String?,
    categories: List<String>,
    categories_tags: List<String>,
    onCategorySelected: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onIncludeLogsChange: (Boolean) -> Unit,
    onSubmit: () -> Unit
) {
    val scrollState = rememberScrollState()

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
            onExpandedChange = onExpandedChange,
            onCategorySelected = onCategorySelected
        )

        DescriptionInput(
            description = description,
            onDescriptionChange = onDescriptionChange
        )

        InfoNote()

        Spacer(modifier = Modifier.height(8.dp))

        SubmitButton(
            enabled = selectedCategory.isNotEmpty() && description.isNotEmpty() && !isSubmitting,
            onClick = onSubmit
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    SubmissionStatusOverlay(
        isSubmitting = isSubmitting,
        isSuccess = isSuccess,
        submissionError = submissionError
    )
} 