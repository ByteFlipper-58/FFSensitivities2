package com.byteflipper.ffsensitivities.ads.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.core.AdType
import com.byteflipper.ffsensitivities.ads.core.BannerAdProvider
import com.byteflipper.ffsensitivities.ads.viewmodel.SimpleAdViewModel

/**
 * Компонент баннерной рекламы для Compose
 */
@Composable
fun AdBanner(
    location: AdLocation = AdLocation.MAIN_BANNER,
    modifier: Modifier = Modifier,
    adViewModel: SimpleAdViewModel = hiltViewModel()
) {
    val isInEditMode = LocalInspectionMode.current
    val context = LocalContext.current

    if (isInEditMode) {
        // Превью для редактора
        PreviewAdBanner(modifier)
        return
    }

    val bannerProvider = remember(location) {
        adViewModel.getAdProvider(AdType.BANNER, location) as? BannerAdProvider
    }

    if (bannerProvider == null) {
        // Если провайдер недоступен, не показываем ничего
        return
    }

    val adView = remember(bannerProvider, context) {
        bannerProvider.createAdView(context as android.app.Activity)
    }

    if (adView != null) {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { adView }
        )
    }
    // Если adView == null, не показываем ничего (пустой компонент)
}

/**
 * Превью баннера для редактора и ошибок
 */
@Composable
private fun PreviewAdBanner(
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(if (isError) Color.Red.copy(alpha = 0.3f) else Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isError) "Ошибка загрузки рекламы" else "Превью баннера",
            color = if (isError) Color.Red else Color.White,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Индикатор состояния рекламы для определенной локации
 */
@Composable
fun AdStatusIndicator(
    location: AdLocation,
    modifier: Modifier = Modifier,
    adViewModel: SimpleAdViewModel = hiltViewModel()
) {
    val adReadyState by adViewModel.adReadyState.collectAsState()
    val isReady = adReadyState[location] ?: false

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isReady) Color.Green.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (isReady) Color.Green else Color.Gray,
                        androidx.compose.foundation.shape.CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${location.name}: ${if (isReady) "Готова" else "Загружается"}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Динамический отступ снизу в зависимости от готовности рекламы
 */
@Composable
fun getDynamicBottomPadding(
    location: AdLocation,
    adViewModel: SimpleAdViewModel = hiltViewModel()
): Dp {
    val adReadyState by adViewModel.adReadyState.collectAsState()
    val isAdReady = adReadyState[location] ?: false
    
    return if (isAdReady) {
        150.dp // Отступ когда баннер видим
    } else {
        80.dp // Минимальный отступ для навигации
    }
}