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
import com.byteflipper.ffsensitivities.ads.core.AdReadyKey
import com.byteflipper.ffsensitivities.ads.core.AdType
import com.byteflipper.ffsensitivities.ads.viewmodel.SimpleAdViewModel

@Composable
fun AppOpenAdSection(
    adViewModel: SimpleAdViewModel,
    activity: Activity?,
    adReadyState: Map<AdReadyKey, Boolean>
) {
    AdSectionCard(
        title = "🚀 App Open реклама",
        description = "Реклама при запуске приложения",
        icon = painterResource(id = R.drawable.rocket_24px),
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
    ) {
        val isReady = adReadyState[AdReadyKey(AdType.APP_OPEN, AdLocation.APP_STARTUP)] == true

        ModernAdButton(
            text = "Показать App Open",
            icon = painterResource(id = R.drawable.open_in_new_24px),
            enabled = isReady,
            isReady = isReady,
            onClick = {
                activity?.let {
                    adViewModel.showAppOpenAd(it) { result ->
                        // Результат обрабатывается автоматически
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatusIndicator(
            isReady = isReady,
            readyText = "App Open реклама готова к показу",
            notReadyText = "App Open реклама загружается..."
        )
    }
} 
