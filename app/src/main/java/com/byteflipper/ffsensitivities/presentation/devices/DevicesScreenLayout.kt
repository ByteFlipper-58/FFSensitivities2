package com.byteflipper.ffsensitivities.presentation.devices

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.activity.compose.LocalActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import android.app.Activity
import com.byteflipper.ffsensitivities.ads.viewmodel.SimpleAdViewModel

@Composable
fun DevicesScreenLayout(
    navController: NavHostController,
    name: String?,
    model: String?,
    onOpenSensitivities: (String, String) -> Unit,
    deviceViewModel: DevicesScreenViewModel = hiltViewModel(),
    adViewModel: SimpleAdViewModel = hiltViewModel()
) {
    val uiState by deviceViewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current as? Activity

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
            adViewModel = adViewModel,
            activity = activity,
            onRetry = { model?.let(deviceViewModel::fetchDevices) }
        )
    }
} 
