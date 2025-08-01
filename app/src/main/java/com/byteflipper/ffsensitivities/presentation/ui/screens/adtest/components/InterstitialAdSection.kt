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
fun InterstitialAdSection(
    adViewModel: UnifiedAdViewModel,
    activity: Activity?,
    adReadyState: Map<AdLocation, Boolean>
) {
    AdSectionCard(
        title = "📱 Интерстициальная реклама",
        description = "Полноэкранная реклама между контентом",
        icon = painterResource(id = R.drawable.fullscreen_24px),
        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernAdButton(
                text = "Главная",
                icon = painterResource(id = R.drawable.home_24px),
                enabled = adReadyState[AdLocation.HOME_SCREEN] == true,
                isReady = adReadyState[AdLocation.HOME_SCREEN] == true,
                modifier = Modifier.weight(1f),
                onClick = {
                    activity?.let {
                        adViewModel.showInterstitialAd(AdLocation.HOME_SCREEN, it)
                    }
                }
            )

            ModernAdButton(
                text = "Устройства",
                icon = painterResource(id = R.drawable.phone_android_24px),
                enabled = adReadyState[AdLocation.DEVICES_SCREEN] == true,
                isReady = adReadyState[AdLocation.DEVICES_SCREEN] == true,
                modifier = Modifier.weight(1f),
                onClick = {
                    activity?.let {
                        adViewModel.showInterstitialAd(AdLocation.DEVICES_SCREEN, it)
                    }
                }
            )
        }
    }
} 