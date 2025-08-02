package com.byteflipper.ffsensitivities.presentation.devices

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.activity.compose.LocalActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import android.app.Activity
import com.byteflipper.ffsensitivities.ads.viewmodel.UnifiedAdViewModel
import com.byteflipper.ffsensitivities.presentation.viewmodel.devices.DevicesScreenViewModel

@Composable
fun DevicesScreenLayout(
    navController: NavHostController,
    name: String?,
    model: String?,
    deviceViewModel: DevicesScreenViewModel = hiltViewModel(),
    adViewModel: UnifiedAdViewModel = hiltViewModel()
) {
    val uiState by deviceViewModel.uiState.collectAsState()
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
            navController = navController,
            adViewModel = adViewModel,
            activity = activity,
            model = model,
            deviceViewModel = deviceViewModel
        )
    }
} 