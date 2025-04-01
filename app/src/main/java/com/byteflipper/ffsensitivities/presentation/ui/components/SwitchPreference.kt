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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
fun SwitchPreference(
    title: String,
    descriptionOn: String = "Enabled",
    descriptionOff: String = "Disabled",
    checked: Boolean,
    icon: Painter? = null,
    activeIndicatorColor: Color = MaterialTheme.colorScheme.primary,
    inactiveIndicatorColor: Color = Color.Gray.copy(alpha = 0.3f),
    activeSwitchColor: Color = MaterialTheme.colorScheme.primary,
    inactiveSwitchColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onCheckedChange: (Boolean) -> Unit
) {
    val indicatorColor = if (checked) activeIndicatorColor else inactiveIndicatorColor

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }, // Make the entire component clickable
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (checked) activeIndicatorColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (checked) descriptionOn else descriptionOff,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .width(3.dp)
                    .height(32.dp)
                    .background(
                        color = indicatorColor,
                        shape = RoundedCornerShape(50)
                    )
            )

            Switch(
                checked = checked,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = activeSwitchColor,
                    checkedBorderColor = activeSwitchColor,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = inactiveSwitchColor,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSwitchWithText() {
    // Update theme call for preview
    FFSensitivitiesTheme(
        themeSetting = "system",
        dynamicColorSetting = false,
        contrastThemeSetting = false
    ) {
        Column(Modifier.padding(16.dp)) {
            SwitchPreference(
                title = "Dark Mode",
                descriptionOn = "Dark theme is activated",
                descriptionOff = "Light theme is activated",
                checked = true,
                icon = painterResource(id = R.drawable.dark_mode_24px),
                activeIndicatorColor = MaterialTheme.colorScheme.primary,
                inactiveIndicatorColor = Color.Gray.copy(alpha = 0.3f),
                onCheckedChange = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            SwitchPreference(
                title = "Notifications",
                descriptionOn = "You will receive notifications",
                descriptionOff = "Notifications are disabled",
                checked = false,
                icon = painterResource(id = R.drawable.settings_24px),
                activeIndicatorColor = Color(0xFF4CAF50),
                inactiveIndicatorColor = Color.LightGray,
                activeSwitchColor = Color(0xFF4CAF50),
                onCheckedChange = {}
            )
        }
    }
}
