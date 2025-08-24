package com.byteflipper.ffsensitivities.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.navigation.Screen
import com.byteflipper.ffsensitivities.presentation.settings.components.SettingsCategoryCard
import com.byteflipper.ffsensitivities.navigation.navigateToLanguageSettings
import com.byteflipper.ffsensitivities.navigation.navigateToPrivacySettings
import com.byteflipper.ffsensitivities.navigation.navigateToThemeSettings
import com.byteflipper.ffsensitivities.navigation.navigateToAdTest

@Composable
fun SettingsScreenContent(
    navController: NavHostController,
    privacyOptionsRequired: Boolean,
    isDebugMode: Boolean
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsCategoryCard(
            title = stringResource(id = R.string.theme_settings_category_title),
            description = stringResource(id = R.string.theme_settings_card_description),
            iconRes = R.drawable.palette_24px,
            onClick = { navController.navigateToThemeSettings() }
        )

        SettingsCategoryCard(
            title = stringResource(id = R.string.change_language),
            description = stringResource(id = R.string.change_language_description),
            iconRes = R.drawable.translate_24px,
            onClick = { navController.navigateToLanguageSettings() }
        )

        if (privacyOptionsRequired) {
            SettingsCategoryCard(
                title = stringResource(id = R.string.privacy_settings_category_title),
                description = stringResource(id = R.string.privacy_settings_card_description),
                iconRes = R.drawable.privacy_tip_24px,
                onClick = { navController.navigateToPrivacySettings() }
            )
        }

        if (isDebugMode) {
            SettingsCategoryCard(
                title = stringResource(id = R.string.ad_test_title),
                description = stringResource(id = R.string.ad_test_description),
                iconRes = R.drawable.palette_24px,
                onClick = { navController.navigateToAdTest() }
            )
        }
    }
} 