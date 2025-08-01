package com.byteflipper.ffsensitivities.presentation.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.BuildConfig
import android.app.Activity

@Composable
fun SettingsScreenLayout(
    navController: NavHostController,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val consentManager = appViewModel.consentManager
    val privacyOptionsRequired = remember(activity, consentManager) {
        activity != null && consentManager.isPrivacyOptionsRequired
    }

    SettingsScreenScaffold(
        navController = navController
    ) {
        SettingsScreenContent(
            navController = navController,
            privacyOptionsRequired = privacyOptionsRequired,
            isDebugMode = BuildConfig.DEBUG
        )
    }
} 