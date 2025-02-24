package com.byteflipper.ffsensitivities.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.R
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.radioButtonPreference
import me.zhanghai.compose.preference.twoTargetSwitchPreference

@Composable
@Preview(showBackground = true)
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onThemeChange: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val appViewModel: AppViewModel = viewModel(factory = AppViewModel.AppViewModelFactory(context.applicationContext as android.app.Application))

    val dynamicColorState by appViewModel.dynamicColor.collectAsState()
    val contrastThemeState by appViewModel.contrastTheme.collectAsState()
    val selectedTheme by appViewModel.theme.collectAsState()
    val selectedLanguage by appViewModel.language.collectAsState()

    ProvidePreferenceLocals {

        LazyColumn(modifier = Modifier.fillMaxSize()) {

            preferenceCategory(
                key = "general_settings_category",
                title = { Text(text = "General Settings") }
            )

            preferenceCategory(
                key = "language_settings_category",
                title = { Text(text = "Language Settings") }
            )

            radioButtonPreference(
                key = "language_english",
                selected = selectedLanguage == "pl",
                title = { Text(text = "English") },
                summary = { Text(text = "Set app language to English") },
                onClick = {
                    appViewModel.setLanguage("en")
                }
            )

            radioButtonPreference(
                key = "language_russian",
                selected = selectedLanguage == "fr",
                title = { Text(text = "Francus") },
                summary = { Text(text = "Установить francus язык для приложения") },
                onClick = {
                    appViewModel.setLanguage("ru")
                }
            )

            radioButtonPreference(
                key = "language_russian",
                selected = selectedLanguage == "ru",
                title = { Text(text = "Русский") },
                summary = { Text(text = "Установить русский язык для приложения") },
                onClick = {
                    appViewModel.setLanguage("ru")
                }
            )

            item { HorizontalDivider() }

            preferenceCategory(
                key = "theme_settings_category",
                title = { Text(text = "Theme Settings") }
            )

            twoTargetSwitchPreference(
                key = "dynamic_colors",
                defaultValue = false,
                title = { isChecked ->
                    Text(if (isChecked) stringResource(R.string.switch_use_dynamic_colors_title) else stringResource(R.string.switch_use_dynamic_colors_title))
                },
                icon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = null) },
                summary = { isChecked ->
                    Text(if (isChecked) stringResource(R.string.switch_use_dynamic_colors_subtitle_on) else stringResource(R.string.switch_use_dynamic_colors_subtitle_off))
                },
                switchEnabled = { true },
                onClick = { isChecked ->
                    appViewModel.setDynamicColor(isChecked)
                },
            )

            item { HorizontalDivider() }

            radioButtonPreference(
                key = "theme_system",
                selected = selectedTheme == "system",
                title = { Text(text = stringResource(R.string.system_theme)) },
                summary = { Text(text = stringResource(R.string.system_theme_description)) },
                onClick = {
                    appViewModel.setTheme("system")
                    onThemeChange("system")
                }
            )

            radioButtonPreference(
                key = "theme_light",
                selected = selectedTheme == "light",
                title = { Text(text = stringResource(R.string.light_theme)) },
                summary = { Text(text = stringResource(R.string.light_theme_description)) },
                onClick = {
                    appViewModel.setTheme("light")
                    onThemeChange("light")
                }
            )

            radioButtonPreference(
                key = "theme_dark",
                selected = selectedTheme == "dark",
                title = { Text(text = stringResource(R.string.night_theme)) },
                summary = { Text(text = stringResource(R.string.night_theme_description)) },
                onClick = {
                    appViewModel.setTheme("dark")
                    onThemeChange("dark")
                }
            )

            twoTargetSwitchPreference(
                key = "contrast_theme",
                defaultValue = false,
                title = { isChecked ->
                    Text(if (isChecked) "Contrast theme enabled" else "Contrast theme disabled")
                },
                icon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = null) },
                summary = { isChecked ->
                    Text(if (isChecked) "Contrast theme is currently enabled." else "Contrast theme is currently disabled.")
                },
                switchEnabled = { true },
                onClick = { isChecked ->
                    appViewModel.setContrastTheme(isChecked)
                },
            )
        }
    }
}