package com.byteflipper.ffsensitivities.presentation.ui.screens.sensitivities

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.byteflipper.ffsensitivities.presentation.viewmodel.sensitivities.SensitivitiesScreenViewModel

@Composable
fun SensitivitiesScreen(
    navController: NavController,
    manufacturerArg: String?,
    modelNameArg: String?,
    sensitivitiesViewModel: SensitivitiesScreenViewModel = hiltViewModel()
) {
    SensitivitiesScreenLayout(
        navController = navController,
        manufacturerArg = manufacturerArg,
        modelNameArg = modelNameArg
    )
} 