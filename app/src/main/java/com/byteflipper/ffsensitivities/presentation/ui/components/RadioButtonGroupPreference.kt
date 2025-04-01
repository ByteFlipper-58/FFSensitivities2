/*
 * Copyright (c) 2025 ByteFlipper
 * Developed by ByteFlipper for FF Sensitivities application
 * All rights reserved.
 */

package com.byteflipper.ffsensitivities.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.presentation.ui.theme.FFSensitivitiesTheme

data class RadioOption(
    val key: String,
    val title: String,
    val summary: String,
    val description: String? = null,
    val icon: Painter? = null,
    val summaryIcon: ImageVector? = null
)

@Composable
fun RadioButtonGroup(
    options: List<RadioOption>,
    selectedKey: String,
    activeIndicatorColor: Color = MaterialTheme.colorScheme.primary,
    inactiveIndicatorColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    onOptionSelected: (String) -> Unit
) {
    val selectedOption = options.find { it.key == selectedKey } ?: options.firstOrNull()

    Column {
        options.forEachIndexed { index, option ->
            RadioButtonPreference(
                key = option.key,
                title = option.title,
                description = option.description,
                selected = selectedKey == option.key,
                icon = option.icon,
                activeIndicatorColor = activeIndicatorColor,
                inactiveIndicatorColor = inactiveIndicatorColor,
                onClick = { onOptionSelected(option.key) }
            )

            if (index < options.size - 1) {
                Spacer(modifier = Modifier.height(1.dp))
            }
        }

        selectedOption?.let { option ->
            Spacer(modifier = Modifier.height(0.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                option.summaryIcon?.let { summaryIcon ->
                    Icon(
                        imageVector = summaryIcon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Text(
                    text = option.summary,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRadioButtonGroup() {
    // Update theme call for preview
    FFSensitivitiesTheme(
        themeSetting = "system",
        dynamicColorSetting = false,
        contrastThemeSetting = false
    ) {
        val themeOptions = listOf(
            RadioOption(
                key = "theme_system",
                title = "System Theme",
                description = "System default option",
                summary = "Follow system settings for light or dark theme based on your device settings",
                icon = painterResource(id = R.drawable.night_sight_auto_24px),
                summaryIcon = Icons.Outlined.Info
            ),
            RadioOption(
                key = "theme_light",
                title = "Light Theme",
                description = "Bright display",
                summary = "Always use light theme regardless of system settings",
                icon = painterResource(id = R.drawable.light_mode_24px),
                summaryIcon = Icons.Outlined.Info
            ),
            RadioOption(
                key = "theme_dark",
                title = "Dark Theme",
                description = "Dark display for night usage",
                summary = "Always use dark theme regardless of system settings",
                icon = painterResource(id = R.drawable.dark_mode_24px),
                summaryIcon = Icons.Outlined.Info
            )
        )

        Column(modifier = Modifier.padding(16.dp)) {
            RadioButtonGroup(
                options = themeOptions,
                selectedKey = "theme_light",
                onOptionSelected = {}
            )
        }
    }
}
