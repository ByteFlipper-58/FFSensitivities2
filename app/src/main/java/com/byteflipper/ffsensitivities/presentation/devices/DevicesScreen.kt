package com.byteflipper.ffsensitivities.presentation.devices

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.navigation.navigateToSensitivities

@Composable
fun DevicesScreen(
    navController: NavHostController,
    name: String?,
    model: String?,
    devicesViewModel: DevicesScreenViewModel = hiltViewModel()
) {
    DevicesScreenLayout(
        navController = navController,
        name = name,
        model = model,
        onOpenSensitivities = navController::navigateToSensitivities,
        deviceViewModel = devicesViewModel
    )
} 
