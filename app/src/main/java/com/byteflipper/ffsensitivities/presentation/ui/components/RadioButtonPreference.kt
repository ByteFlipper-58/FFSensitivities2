/*
 * Copyright (c) 2025 ByteFlipper
 * Developed by ByteFlipper for FF Sensitivities application
 * All rights reserved.
 */

package com.byteflipper.ffsensitivities.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.presentation.ui.theme.FFSensitivitiesTheme

@Composable
fun RadioButtonPreference(
    title: String,
    selected: Boolean,
    key: String,
    description: String? = null,
    icon: Painter? = null,
    activeIndicatorColor: Color = MaterialTheme.colorScheme.primary,
    inactiveIndicatorColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    onClick: () -> Unit
) {
    val indicatorColor = if (selected) activeIndicatorColor else inactiveIndicatorColor

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (selected) activeIndicatorColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (description != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(2.dp)
                    .height(24.dp)
                    .background(
                        color = indicatorColor,
                        shape = RoundedCornerShape(50)
                    )
            )

            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = activeIndicatorColor,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRadioButtonPreference() {
    // Update theme call for preview
    FFSensitivitiesTheme(
        themeSetting = "system",
        dynamicColorSetting = false,
        contrastThemeSetting = false
    ) {
        Column(Modifier.padding(16.dp)) {
            RadioButtonPreference(
                key = "theme_system",
                title = "System Theme",
                description = "Follow system settings for light or dark theme",
                selected = true,
                icon = painterResource(id = R.drawable.night_sight_auto_24px),
                activeIndicatorColor = MaterialTheme.colorScheme.primary,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            RadioButtonPreference(
                key = "theme_light",
                title = "Light Theme",
                description = null,
                selected = false,
                icon = painterResource(id = R.drawable.light_mode_24px),
                activeIndicatorColor = MaterialTheme.colorScheme.primary,
                onClick = {}
            )
        }
    }
}
