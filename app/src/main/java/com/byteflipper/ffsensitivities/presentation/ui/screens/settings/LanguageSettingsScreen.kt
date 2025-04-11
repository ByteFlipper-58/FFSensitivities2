package com.byteflipper.ffsensitivities.presentation.ui.screens.settings

// Removed Log import
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow // Re-import FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape // Import RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check // Import Check icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Re-import LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.manager.Language
import com.byteflipper.ffsensitivities.manager.appLanguages
import com.byteflipper.ffsensitivities.presentation.ui.findActivity // Re-import findActivity
import com.byteflipper.ui_components.preferences.PreferenceCategory

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LanguageSettingsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.change_language)) },
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
                    title = stringResource(R.string.change_language),
                    icon = painterResource(id = R.drawable.translate_24px),
                )
            }

            item {
                // Combine system default with app languages
                val systemLanguageOption = Language("system", stringResource(R.string.system_default))
                val availableLanguages = listOf(systemLanguageOption) + appLanguages

                // Get current language from ViewModel state
                val currentLanguageCode by appViewModel.currentLanguageCode.collectAsState()

                // Use FlowRow again
                FlowRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp) // Reduced vertical spacing
                ) {
                    val context = LocalContext.current
                    val activity = context.findActivity() // Get activity context again

                    availableLanguages.forEach { language ->
                        FilterChip(
                            selected = currentLanguageCode == language.code,
                            onClick = {
                                // Call ViewModel to change language
                                appViewModel.setLanguage(language.code)
                                // Recreate activity with fade animation
                                activity?.let { act ->
                                    act.recreate()
                                    act.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                                }
                            },
                            label = { Text(language.displayLanguage) },
                            shape = RoundedCornerShape(16.dp), // Apply rounded corners
                            leadingIcon = if (currentLanguageCode == language.code) { // Show check only if selected
                                { // Composable lambda for the icon
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "stringResource(R.string.selected)" // Accessibility
                                    )
                                }
                            } else {
                                null // No icon if not selected
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// Removed the old getDisplayName function
