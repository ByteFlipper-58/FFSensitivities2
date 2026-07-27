package com.byteflipper.ffsensitivities.presentation.settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.BuildConfig
import com.byteflipper.ffsensitivities.navigation.navigateToAdTest
import com.byteflipper.ffsensitivities.navigation.navigateToLanguageSettings
import com.byteflipper.ffsensitivities.navigation.navigateToPrivacySettings
import com.byteflipper.ffsensitivities.navigation.navigateToThemeSettings

@Composable
fun SettingsScreenLayout(
    navController: NavHostController,
    privacyViewModel: PrivacyViewModel = hiltViewModel()
) {
    SettingsScreenScaffold(
        navController = navController
    ) {
        SettingsScreenContent(
            onOpenThemeSettings = { navController.navigateToThemeSettings() },
            onOpenLanguageSettings = { navController.navigateToLanguageSettings() },
            onOpenPrivacySettings = { navController.navigateToPrivacySettings() },
            onOpenAdTest = { navController.navigateToAdTest() },
            isDebugMode = BuildConfig.DEBUG
        )
    }
}
