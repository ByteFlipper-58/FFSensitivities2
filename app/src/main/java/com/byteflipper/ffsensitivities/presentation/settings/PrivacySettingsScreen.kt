package com.byteflipper.ffsensitivities.presentation.settings

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ui_components.preferences.PreferenceCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val adManager = appViewModel.adManager

    // State to potentially update UI based on form dismissal, though the button visibility is primary
    var privacyOptionsRequired by remember { mutableStateOf(activity != null && adManager.isPrivacyOptionsRequired) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.privacy_settings_category_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                PreferenceCategory(
                    title = stringResource(R.string.privacy_settings_category_title),
                    icon = painterResource(id = R.drawable.privacy_tip_24px)
                )
            }

            item {
                // Show the privacy options button only if required and activity context is available
                if (privacyOptionsRequired && activity != null) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.privacy_settings_button_title)) },
                        supportingContent = { Text(stringResource(R.string.privacy_settings_button_description)) },
                        leadingContent = {
                            Icon(
                                painter = painterResource(id = R.drawable.manage_accounts_24px),
                                contentDescription = null // Decorative icon
                            )
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp) // Add padding to match other items if needed
                            .clip(MaterialTheme.shapes.medium) // Optional: Add clipping for ripple effect
                            .clickable {
                                adManager.showPrivacyOptionsForm(activity) { formError ->
                                    if (formError != null) {
                                        // Handle error, e.g., show a Snackbar
                                        // Log.e("PrivacySettingsScreen", "Error showing privacy options form: ${formError.message}")
                                    } else {
                                        // Form dismissed, AdManager already updated SDKs.
// Re-check requirement status after dismissal if necessary for UI updates.
privacyOptionsRequired = adManager.isPrivacyOptionsRequired
                                    }
                                }
                            }
                    )
                    Spacer(modifier = Modifier.height(8.dp)) // Add space after the item
                } else {
                    // Optionally show a message if privacy options are not available/required
                     ListItem( // Use ListItem for consistent appearance
                         headlineContent = { Text(stringResource(R.string.privacy_settings_not_required_title)) }, // Add this string
                         supportingContent = { Text(stringResource(R.string.privacy_settings_not_required_description)) }, // Add this string
                         leadingContent = {
                             Icon(
                                 painter = painterResource(id = R.drawable.info_24px), // Use an info icon
                                 contentDescription = null
                             )
                         },
                         modifier = Modifier.padding(horizontal = 16.dp)
                     )
                     Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
