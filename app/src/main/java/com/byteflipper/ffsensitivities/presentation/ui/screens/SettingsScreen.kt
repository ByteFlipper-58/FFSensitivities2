package com.byteflipper.ffsensitivities.presentation.ui.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background // Import background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape // Import CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward // Import forward arrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color // Import Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import android.app.Activity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val consentManager = appViewModel.consentManager
    val privacyOptionsRequired = remember(activity, consentManager) {
        activity != null && consentManager.isPrivacyOptionsRequired
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        // Use Column instead of LazyColumn if the number of categories is small and fixed
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp), // Add padding around the column content
            verticalArrangement = Arrangement.spacedBy(16.dp) // Space between cards
        ) {
            // --- Theme Settings Card ---
            SettingsCategoryCard(
                title = stringResource(R.string.theme_settings_category_title),
                description = stringResource(R.string.theme_settings_card_description), // New string
                iconRes = R.drawable.palette_24px,
                onClick = { navController.navigate(Screen.ThemeSettings.route) }
            )

            // --- Language Settings Card ---
             SettingsCategoryCard(
                title = stringResource(R.string.change_language),
                description = stringResource(R.string.language_settings_card_description), // New string
                iconRes = R.drawable.translate_24px,
                onClick = { navController.navigate(Screen.LanguageSettings.route) } // Uncommented navigation
            )

            // --- Privacy Settings Card ---
            // Show only if privacy options are potentially needed
            if (privacyOptionsRequired) {
                SettingsCategoryCard(
                    title = stringResource(R.string.privacy_settings_category_title),
                    description = stringResource(R.string.privacy_settings_card_description), // New string
                    iconRes = R.drawable.privacy_tip_24px,
                    onClick = { navController.navigate(Screen.PrivacySettings.route) }
                )
            }
        }
    }
}

@Composable
private fun SettingsCategoryCard(
    title: String,
    description: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium) // Clip the row for ripple effect
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp), // Adjust padding
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Box for circular background
        Box(
            modifier = Modifier
                .size(48.dp) // Size of the background circle
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), // Semi-transparent background
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center // Center the icon inside the Box
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null, // Decorative
                modifier = Modifier.size(28.dp), // Smaller icon size
                tint = MaterialTheme.colorScheme.onPrimaryContainer // Icon color contrasting with background
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium // Smaller title
            )
            Spacer(modifier = Modifier.height(2.dp)) // Reduced spacer
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall, // Smaller body text
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Add arrow icon at the end
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null, // Decorative
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) // Subtle color
        )
    }
}
