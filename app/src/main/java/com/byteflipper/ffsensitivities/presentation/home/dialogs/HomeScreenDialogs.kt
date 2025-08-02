package com.byteflipper.ffsensitivities.presentation.home.dialogs

import androidx.compose.runtime.Composable
import com.byteflipper.ffsensitivities.presentation.ui.dialogs.SensitivitiesRequestDialog

@Composable
fun HomeScreenDialogs(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onRequestSent: () -> Unit
) {
    if (showDialog) {
        SensitivitiesRequestDialog(
            onDismiss = onDismiss,
            onRequestSent = onRequestSent
        )
    }
} 