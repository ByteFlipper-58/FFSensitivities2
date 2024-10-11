package com.byteflipper.ffsensitivities.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import com.byteflipper.ffsensitivities.getDynamicColorPreference
import com.byteflipper.ffsensitivities.getThemePreference
import com.byteflipper.ffsensitivities.saveDynamicColorPreference
import com.byteflipper.ffsensitivities.saveThemePreference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.radioButtonPreference
import me.zhanghai.compose.preference.twoTargetSwitchPreference

@Composable
@Preview(showBackground = true)
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedTheme = getThemePreference(context)
    val dynamicColorEnabled = getDynamicColorPreference(context)

    var switchChecked by remember { mutableStateOf(dynamicColorEnabled) }
    var selectedTheme by remember { mutableStateOf(savedTheme) }

    val switchState = remember { mutableStateOf(true) }

    LaunchedEffect(switchState) {
        saveDynamicColorPreference(context, switchChecked)
        Toast.makeText(context, "Dynamic colors are enabled." + switchChecked.toString(), Toast.LENGTH_SHORT).show()
    }

    ProvidePreferenceLocals {
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            preferenceCategory(
                key = "general_settings_category",
                title = { Text(text = "General Settings") }
            )

            twoTargetSwitchPreference(
                key = "dynamic_colors",
                defaultValue = false,
                title = { isChecked ->
                    Text(if (isChecked) "Dynamic Colors" else "Dynamic Colors")
                },
                icon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = null) },
                summary = { isChecked ->
                    Text(if (isChecked) "Dynamic colors are currently enabled." else "Dynamic colors are currently disabled.")
                },
                onClick = { isChecked ->
                    switchChecked = isChecked
                    saveDynamicColorPreference(context, isChecked)
                    handleDynamicColorChange(context, isChecked)
                }
            )

            preferenceCategory(
                key = "theme_settings_category",
                title = { Text(text = "Theme Settings") }
            )

            radioButtonPreference(
                key = "theme_system",
                selected = selectedTheme == "system",
                title = { Text(text = "System Default") },
                summary = { Text(text = "Use system default theme.") },
                onClick = {
                    selectedTheme = "system"
                    saveThemePreference(context, "system")
                }
            )

            radioButtonPreference(
                key = "theme_light",
                selected = selectedTheme == "light",
                title = { Text(text = "Light Theme") },
                summary = { Text(text = "Use light theme.") },
                onClick = {
                    selectedTheme = "light"
                    saveThemePreference(context, "light")
                }
            )

            radioButtonPreference(
                key = "theme_dark",
                selected = selectedTheme == "dark",
                title = { Text(text = "Dark Theme") },
                summary = { Text(text = "Use dark theme.") },
                onClick = {
                    selectedTheme = "dark"
                    saveThemePreference(context, "dark")
                }
            )
        }
    }
}

fun handleDynamicColorChange(context: Context, isEnabled: Boolean) {
    val message = if (isEnabled) {
        "Dynamic colors are enabled."
    } else {
        "Dynamic colors are disabled."
    }
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}