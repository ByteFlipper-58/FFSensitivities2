package com.byteflipper.ffsensitivities.presentation.ui.screens.adtest.components

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.viewmodel.UnifiedAdViewModel

@Composable
fun ActionTrackingSection(
    adViewModel: UnifiedAdViewModel,
    activity: Activity?
) {
    AdSectionCard(
        title = "📊 Трекинг действий",
        description = "Отслеживание действий для показа рекламы по частоте",
        icon = painterResource(id = R.drawable.analytics_24px),
        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
    ) {
        Text(
            text = "Эти кнопки отслеживают действия и показывают рекламу согласно настроенной частоте:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernAdButton(
                text = "Действие 1",
                icon = painterResource(id = R.drawable.touch_app_24px),
                modifier = Modifier.weight(1f),
                onClick = {
                    activity?.let {
                        adViewModel.trackActionAndShowInterstitial(AdLocation.HOME_SCREEN, it)
                    }
                }
            )

            ModernAdButton(
                text = "Действие 2",
                icon = painterResource(id = R.drawable.touch_app_24px),
                modifier = Modifier.weight(1f),
                onClick = {
                    activity?.let {
                        adViewModel.trackActionAndShowInterstitial(AdLocation.DEVICES_SCREEN, it)
                    }
                }
            )
        }
    }
} 