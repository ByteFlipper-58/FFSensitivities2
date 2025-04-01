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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
fun PreferenceCategory(
    title: String,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    description: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Optional icon
            if (icon != null) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Category title
            Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }

        // Optional description
        if (description != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                letterSpacing = 0.25.sp,
                modifier = Modifier.padding(start = if (icon != null) 28.dp else 0.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreferenceCategoryPreview() {
    FFSensitivitiesTheme(
        themeSetting = "system",
        dynamicColorSetting = false,
        contrastThemeSetting = false
    ) {
        Column {
            PreferenceCategory(
                title = "Theme Settings",
                icon = painterResource(id = R.drawable.palette_24px),
                description = "Customize the appearance of the app"
            )

            Spacer(modifier = Modifier.height(16.dp))

            PreferenceCategory(
                title = "General Settings",
                icon = painterResource(id = R.drawable.settings_24px)
            )

            Spacer(modifier = Modifier.height(16.dp))

            PreferenceCategory(
                title = "Advanced Settings",
                description = "Settings for advanced users"
            )
        }
    }
}
