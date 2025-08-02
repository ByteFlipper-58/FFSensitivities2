package com.byteflipper.ffsensitivities.presentation.settings

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

    val adManager = appViewModel.adManager
    val privacyOptionsRequired = remember(activity, adManager) {
    activity != null && adManager.isPrivacyOptionsRequired
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