package com.byteflipper.ffsensitivities.presentation.devices

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

@Composable
fun DevicesScreenLayout(
    navController: NavHostController,
    name: String?,
    model: String?,
    onOpenSensitivities: (String, String) -> Unit,
    deviceViewModel: DevicesScreenViewModel = hiltViewModel()
) {
    val uiState by deviceViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(model) {
        model?.let {
            deviceViewModel.fetchDevices(it)
        }
    }

    DevicesScreenScaffold(
        navController = navController,
        title = name ?: "Устройства"
    ) {
        DevicesScreenContent(
            uiState = uiState,
            onOpenSensitivities = onOpenSensitivities,
            onRetry = { model?.let(deviceViewModel::fetchDevices) }
        )
    }
} 
