package com.byteflipper.ffsensitivities.presentation.ui.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.R
// Updated imports for moved components
import com.byteflipper.ui_components.preferences.PreferenceCategory
import com.byteflipper.ui_components.preferences.RadioButtonGroup
import com.byteflipper.ui_components.preferences.RadioOption
import com.byteflipper.ui_components.preferences.SwitchPreference
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val dynamicColorState by appViewModel.dynamicColor.collectAsState()
    val contrastThemeState by appViewModel.contrastTheme.collectAsState()
    val selectedTheme by appViewModel.theme.collectAsState()

    val currentLocaleTag = remember { AppCompatDelegate.getApplicationLocales().toLanguageTags().ifEmpty { "system" } }

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
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                PreferenceCategory(
                    title = stringResource(R.string.theme_settings_category_title),
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
                        description = stringResource(R.string.theme_system_description_detail), // Use resource
                        summary = stringResource(R.string.system_theme_description),
                        icon = painterResource(id = R.drawable.night_sight_auto_24px),
                        summaryIcon = Icons.Outlined.Info
                    ),
                    RadioOption(
                        key = "theme_light",
                        title = stringResource(R.string.light_theme),
                        description = stringResource(R.string.theme_light_description_detail), // Use resource
                        summary = stringResource(R.string.light_theme_description),
                        icon = painterResource(id = R.drawable.light_mode_24px),
                        summaryIcon = Icons.Outlined.Info
                    ),
                    RadioOption(
                        key = "theme_dark",
                        title = stringResource(R.string.night_theme),
                        description = stringResource(R.string.theme_dark_description_detail), // Use resource
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

            /*item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                PreferenceCategory(
                    title = stringResource(R.string.change_language),
                    icon = painterResource(id = R.drawable.translate_24px),
                )
            }

            // Define the language list and FlowRow within the same item
            item {
                val languages = listOf(
                    "system" to stringResource(R.string.system_default),
                    "en" to getDisplayName("en"),
                    "be" to getDisplayName("be"),
                    "de" to getDisplayName("de"),
                    "fr" to getDisplayName("fr"),
                    "pl" to getDisplayName("pl"),
                    "ru" to getDisplayName("ru"),
                    "tr" to getDisplayName("tr"),
                    "uk" to getDisplayName("uk")
                )
                FlowRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    languages.forEach { (code, name) ->
                        FilterChip(
                            selected = currentLocaleTag == code || (currentLocaleTag == "system" && code == "system"),
                            onClick = {
                                val languageToSet = if (code == "system") null else code
                                appViewModel.setLanguage(languageToSet)
                            },
                            label = { Text(name) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }*/
        }
    }
}

private fun getDisplayName(languageCode: String): String {
    val locale = Locale(languageCode)
    // Capitalize the first letter of the display name in its own language
    return locale.getDisplayLanguage(locale).replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(locale) else it.toString()
    }
}
