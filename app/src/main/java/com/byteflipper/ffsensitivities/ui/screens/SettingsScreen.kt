package com.byteflipper.ffsensitivities.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.byteflipper.ffsensitivities.PreferencesManager
import com.byteflipper.ffsensitivities.R
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.radioButtonPreference
import me.zhanghai.compose.preference.rememberPreferenceState
import me.zhanghai.compose.preference.twoTargetSwitchPreference

@Composable
@Preview(showBackground = true)
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onThemeChange: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val preferencesManager = PreferencesManager(context)
    var selectedTheme by remember { mutableStateOf(preferencesManager.readString("theme", "system") ?: "system") }

    ProvidePreferenceLocals {
        var dynamicColorState by rememberPreferenceState("dynamic_colors", false)
        var contrastThemeState by rememberPreferenceState("contrast_level", false)

        LazyColumn(modifier = Modifier.fillMaxSize()) {

            preferenceCategory(
                key = "general_settings_category",
                title = { Text(text = "General Settings") }
            )

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
                onClick = { isChecked ->
                    //preferencesManager.putBoolean("dynamic_colors", isChecked)
                    dynamicColorState = isChecked
                },
            )

            item {
                HorizontalDivider()
            }

            radioButtonPreference(
                key = "theme_system",
                selected = selectedTheme == "system",
                title = { Text(text = stringResource(R.string.system_theme)) },
                summary = { Text(text = stringResource(R.string.system_theme_description)) },
                onClick = {
                    selectedTheme = "system"
                    preferencesManager.putString("theme", "system")
                    onThemeChange(selectedTheme)
                }
            )

            radioButtonPreference(
                key = "theme_light",
                selected = selectedTheme == "light",
                title = { Text(text = stringResource(R.string.light_theme)) },
                summary = { Text(text = stringResource(R.string.light_theme_description)) },
                onClick = {
                    selectedTheme = "light"
                    preferencesManager.putString("theme", "light")
                    onThemeChange(selectedTheme)
                }
            )

            radioButtonPreference(
                key = "theme_dark",
                selected = selectedTheme == "dark",
                title = { Text(text = stringResource(R.string.night_theme)) },
                summary = { Text(text = stringResource(R.string.night_theme_description)) },
                onClick = {
                    selectedTheme = "dark"
                    preferencesManager.putString("theme", "dark")
                    onThemeChange(selectedTheme)
                }
            )

            /*twoTargetSwitchPreference(
                key = "contrast_theme",
                defaultValue = false,
                title = { Text("Contrast Theme") },
                icon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = null) },
                summary = { isChecked ->
                    Text(if (isChecked) "Contrast theme is currently enabled." else "Contrast theme is currently disabled.")
                },
                onClick = { isChecked ->
                    //preferencesManager.putBoolean("dynamic_colors", isChecked)
                    contrastThemeState = isChecked
                },
            )*/
        }
    }
}