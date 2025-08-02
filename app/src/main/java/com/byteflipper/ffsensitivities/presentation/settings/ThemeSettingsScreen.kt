package com.byteflipper.ffsensitivities.presentation.settings

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
import com.byteflipper.ui_components.preferences.PreferenceCategory
import com.byteflipper.ui_components.preferences.RadioButtonGroup
import com.byteflipper.ui_components.preferences.RadioOption
import com.byteflipper.ui_components.preferences.SwitchPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val dynamicColorState by appViewModel.dynamicColor.collectAsState()
    val contrastThemeState by appViewModel.contrastTheme.collectAsState()
    val selectedTheme by appViewModel.theme.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.theme_settings_category_title)) },
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
                    title = stringResource(R.string.theme_settings_category_title), // Re-add category title for clarity within the screen
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
                        description = stringResource(R.string.theme_system_description_detail),
                        summary = stringResource(R.string.system_theme_description),
                        icon = painterResource(id = R.drawable.night_sight_auto_24px),
                        summaryIcon = Icons.Outlined.Info
                    ),
                    RadioOption(
                        key = "theme_light",
                        title = stringResource(R.string.light_theme),
                        description = stringResource(R.string.theme_light_description_detail),
                        summary = stringResource(R.string.light_theme_description),
                        icon = painterResource(id = R.drawable.light_mode_24px),
                        summaryIcon = Icons.Outlined.Info
                    ),
                    RadioOption(
                        key = "theme_dark",
                        title = stringResource(R.string.night_theme),
                        description = stringResource(R.string.theme_dark_description_detail),
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
                    }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                SwitchPreference(
                    title = stringResource(R.string.contrast_theme_title),
                    descriptionOn = stringResource(R.string.contrast_theme_description_on),
                    descriptionOff = stringResource(R.string.contrast_theme_description_off),
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
