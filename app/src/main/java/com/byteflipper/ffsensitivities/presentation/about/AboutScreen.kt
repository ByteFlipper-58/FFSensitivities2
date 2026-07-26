package com.byteflipper.ffsensitivities.presentation.about

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.navigation.navigateToBugReport

@Composable
fun AboutScreen(navController: NavHostController) {
    AboutScreenLayout(
        navController = navController,
        onOpenBugReport = navController::navigateToBugReport
    )
} 
