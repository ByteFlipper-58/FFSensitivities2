package com.byteflipper.ffsensitivities.presentation.adtest.components

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
fun RewardedAdSection(
    adViewModel: UnifiedAdViewModel,
    activity: Activity?,
    adReadyState: Map<AdLocation, Boolean>
) {
    AdSectionCard(
        title = "🎁 Наградная реклама",
        description = "Получите награду за просмотр рекламы",
        icon = painterResource(id = R.drawable.featured_seasonal_and_gifts_24px),
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    ) {
        ModernAdButton(
            text = "Получить награду",
            icon = painterResource(id = R.drawable.redeem_24px),
            enabled = adReadyState[AdLocation.PREMIUM_FEATURES] == true,
            isReady = adReadyState[AdLocation.PREMIUM_FEATURES] == true,
            onClick = {
                activity?.let {
                    adViewModel.showRewardedAd(AdLocation.PREMIUM_FEATURES, it) { result ->
                        if (result.success && result.reward != null) {
                            // Награда получена
                        }
                    }
                }
            }
        )
    }
} 