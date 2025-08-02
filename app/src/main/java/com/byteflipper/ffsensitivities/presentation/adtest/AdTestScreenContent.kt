package com.byteflipper.ffsensitivities.presentation.adtest

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.core.AdResult
import com.byteflipper.ffsensitivities.ads.components.getDynamicBottomPadding
import com.byteflipper.ffsensitivities.ads.viewmodel.UnifiedAdViewModel
import com.byteflipper.ffsensitivities.presentation.ui.screens.adtest.components.AdStatusCard
import com.byteflipper.ffsensitivities.presentation.ui.screens.adtest.components.AdResultCard
import com.byteflipper.ffsensitivities.presentation.ui.screens.adtest.components.AppOpenAdSection
import com.byteflipper.ffsensitivities.presentation.ui.screens.adtest.components.InterstitialAdSection
import com.byteflipper.ffsensitivities.presentation.ui.screens.adtest.components.RewardedAdSection
import com.byteflipper.ffsensitivities.presentation.ui.screens.adtest.components.ActionTrackingSection
import com.byteflipper.ffsensitivities.presentation.ui.screens.adtest.components.FrequencyInfoCard

@Composable
fun AdTestScreenContent(
    adViewModel: UnifiedAdViewModel,
    activity: Activity?,
    adReadyState: Map<AdLocation, Boolean>,
    lastAdResult: AdResult?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = getDynamicBottomPadding(AdLocation.SETTINGS_SCREEN, adViewModel)),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Статус готовности рекламы
        AdStatusCard(adReadyState = adReadyState)

        // Последний результат
        lastAdResult?.let { result ->
            AdResultCard(result = result)
        }

        // App Open реклама
        AppOpenAdSection(
            adViewModel = adViewModel,
            activity = activity,
            adReadyState = adReadyState
        )

        // Интерстициальная реклама
        InterstitialAdSection(
            adViewModel = adViewModel,
            activity = activity,
            adReadyState = adReadyState
        )

        // Наградная реклама
        RewardedAdSection(
            adViewModel = adViewModel,
            activity = activity,
            adReadyState = adReadyState
        )

        // Трекинг действий
        ActionTrackingSection(
            adViewModel = adViewModel,
            activity = activity
        )

        // Информация о частоте
        FrequencyInfoCard()

        Spacer(modifier = Modifier.height(16.dp))
    }
} 