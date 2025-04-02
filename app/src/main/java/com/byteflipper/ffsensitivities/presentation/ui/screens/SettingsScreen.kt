package com.byteflipper.ffsensitivities.presentation.ui.screens

// import androidx.lifecycle.viewmodel.compose.viewModel // Use hiltViewModel instead
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.presentation.ui.components.PreferenceCategory
import com.byteflipper.ffsensitivities.presentation.ui.components.RadioButtonGroup
import com.byteflipper.ffsensitivities.presentation.ui.components.RadioOption
import com.byteflipper.ffsensitivities.presentation.ui.components.SwitchPreference

@OptIn(ExperimentalMaterial3Api::class) // Add OptIn for TopAppBar
@Composable
// @Preview(showBackground = true) // Preview might fail without Hilt context
fun SettingsScreen(
    navController: NavHostController, // Add navController parameter
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = hiltViewModel() // Get ViewModel using Hilt
) {
    val dynamicColorState by appViewModel.dynamicColor.collectAsState()
    val contrastThemeState by appViewModel.contrastTheme.collectAsState()
    val selectedTheme by appViewModel.theme.collectAsState()
    // val selectedLanguage by appViewModel.language.collectAsState() // Language settings seem removed, keep commented

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
        LazyColumn(
            modifier = modifier // Use passed modifier
                .fillMaxSize()
                .padding(innerPadding) // Apply padding from Scaffold
        ) {
            /*item {
                PreferenceCategory(
                title = "General Settings",
                icon = painterResource(id = R.drawable.settings_24px),
            )
        }*/

            item {
                PreferenceCategory(
                    title = "Theme Settings",
                    icon = painterResource(id = R.drawable.palette_24px),
                )
            }

            item {
                SwitchPreference(
                    title = stringResource(R.string.switch_use_dynamic_colors_title),
                    descriptionOn = stringResource(R.string.switch_use_dynamic_colors_subtitle_on),
                    descriptionOff = stringResource(R.string.switch_use_dynamic_colors_subtitle_off),
                    checked = dynamicColorState,
                    icon = painterResource(id = R.drawable.colors_24px),
                    activeIndicatorColor = MaterialTheme.colorScheme.primary,
                    inactiveIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    onCheckedChange = { isChecked ->
                        appViewModel.setDynamicColor(isChecked)
                    }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                val themeOptions = listOf(
                    RadioOption(
                        key = "theme_system",
                        title = stringResource(R.string.system_theme),
                        description = "Default system option", // Added description
                        summary = stringResource(R.string.system_theme_description),
                        icon = painterResource(id = R.drawable.night_sight_auto_24px),
                        summaryIcon = Icons.Outlined.Info
                    ),
                    RadioOption(
                        key = "theme_light",
                        title = stringResource(R.string.light_theme),
                        description = "Bright theme for better visibility", // Added description
                        summary = stringResource(R.string.light_theme_description),
                        icon = painterResource(id = R.drawable.light_mode_24px),
                        summaryIcon = Icons.Outlined.Info
                    ),
                    RadioOption(
                        key = "theme_dark",
                        title = stringResource(R.string.night_theme),
                        description = "Dark theme to reduce eye strain", // Added description
                        summary = stringResource(R.string.night_theme_description),
                        icon = painterResource(id = R.drawable.dark_mode_24px),
                        summaryIcon = Icons.Outlined.Info
                    )
                )

                RadioButtonGroup(
                    options = themeOptions,
                    selectedKey = when (selectedTheme) {
                        "system" -> "theme_system"
                        "light" -> "theme_light"
                        "dark" -> "theme_dark"
                        else -> "theme_system"
                    },
                    activeIndicatorColor = MaterialTheme.colorScheme.primary,
                    inactiveIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    onOptionSelected = { key ->
                        val theme = when (key) {
                            "theme_system" -> "system"
                            "theme_light" -> "light"
                            "theme_dark" -> "dark"
                            else -> "system"
                        }
                        appViewModel.setTheme(theme)
                        // onThemeChange(theme) // Removed call
                    }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                SwitchPreference(
                    title = "Contrast theme",
                    descriptionOn = "Contrast theme is currently enabled.",
                    descriptionOff = "Contrast theme is currently disabled.",
                    checked = contrastThemeState,
                    icon = painterResource(id = R.drawable.contrast_24px),
                    activeIndicatorColor = MaterialTheme.colorScheme.tertiary,
                    inactiveIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    onCheckedChange = { isChecked ->
                        appViewModel.setContrastTheme(isChecked)
                    }
                )
            }
        }
    }
}
