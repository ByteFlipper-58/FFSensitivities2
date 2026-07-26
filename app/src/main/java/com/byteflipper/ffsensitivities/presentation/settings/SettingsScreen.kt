package com.byteflipper.ffsensitivities.presentation.settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun SettingsScreen(
    navController: NavHostController,
    privacyViewModel: PrivacyViewModel = hiltViewModel()
) {
    SettingsScreenLayout(
        navController = navController,
        privacyViewModel = privacyViewModel
    )
} 
