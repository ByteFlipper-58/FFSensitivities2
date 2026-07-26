package com.byteflipper.ffsensitivities.presentation.sensitivities

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun SensitivitiesScreen(
    navController: NavController,
    manufacturerArg: String?,
    modelNameArg: String?
) {
    SensitivitiesScreenLayout(
        navController = navController,
        manufacturerArg = manufacturerArg,
        modelNameArg = modelNameArg
    )
} 
