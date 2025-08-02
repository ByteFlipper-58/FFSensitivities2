package com.byteflipper.ffsensitivities.presentation.settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.AppViewModel

@Composable
fun SettingsScreen(
    navController: NavHostController,
    appViewModel: AppViewModel = hiltViewModel()
) {
    SettingsScreenLayout(
        navController = navController,
        appViewModel = appViewModel
    )
} 