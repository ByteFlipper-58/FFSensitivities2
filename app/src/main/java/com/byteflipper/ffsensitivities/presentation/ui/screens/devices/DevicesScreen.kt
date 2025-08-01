package com.byteflipper.ffsensitivities.presentation.ui.screens.devices

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.presentation.viewmodel.devices.DevicesScreenViewModel

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
        deviceViewModel = devicesViewModel
    )
} 