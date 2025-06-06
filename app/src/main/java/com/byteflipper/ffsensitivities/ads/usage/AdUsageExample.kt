package com.byteflipper.ffsensitivities.ads.usage

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.byteflipper.ffsensitivities.ads.components.AdBanner
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.core.AdType
import com.byteflipper.ffsensitivities.ads.viewmodel.UnifiedAdViewModel

/**
 * Примеры использования новой архитектуры рекламы
 */
@Composable
fun AdUsageExamples(
    modifier: Modifier = Modifier,
    adViewModel: UnifiedAdViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    val adReadyState by adViewModel.adReadyState.collectAsState()
    val lastAdResult by adViewModel.lastAdResult.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Примеры использования рекламы",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        // Баннерная реклама
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Баннерная реклама",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Баннер главной страницы
                    AdBanner(location = AdLocation.MAIN_BANNER)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Баннер экрана устройств
                    Text("Баннер экрана устройств:")
                    AdBanner(location = AdLocation.DEVICES_SCREEN)
                }
            }
        }

        // Интерстициальная реклама
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Интерстициальная реклама",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                activity?.let { 
                                    adViewModel.showInterstitialAd(AdLocation.HOME_SCREEN, it)
                                }
                            },
                            enabled = adReadyState[AdLocation.HOME_SCREEN] == true
                        ) {
                            Text("Главная")
                        }
                        
                        Button(
                            onClick = {
                                activity?.let { 
                                    adViewModel.showInterstitialAd(AdLocation.DEVICES_SCREEN, it)
                                }
                            },
                            enabled = adReadyState[AdLocation.DEVICES_SCREEN] == true
                        ) {
                            Text("Устройства")
                        }
                        
                        Button(
                            onClick = {
                                activity?.let { 
                                    adViewModel.trackActionAndShowInterstitial(AdLocation.SENSITIVITIES_SCREEN, it)
                                }
                            }
                        ) {
                            Text("Трекинг")
                        }
                    }
                }
            }
        }

        // Наградная реклама
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Наградная реклама",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                activity?.let { 
                                    adViewModel.showRewardedAd(AdLocation.PREMIUM_FEATURES, it) { result ->
                                        // Обработка награды
                                        result.reward?.let { reward ->
                                            // Пользователь получил награду
                                            println("Награда получена: ${reward.amount} ${reward.type}")
                                        }
                                    }
                                }
                            },
                            enabled = adReadyState[AdLocation.PREMIUM_FEATURES] == true
                        ) {
                            Text("Премиум")
                        }
                        
                        Button(
                            onClick = {
                                activity?.let { 
                                    adViewModel.showRewardedAd(AdLocation.EXTRA_SENSITIVITIES, it) { result ->
                                        result.reward?.let { reward ->
                                            println("Дополнительные настройки: ${reward.amount} ${reward.type}")
                                        }
                                    }
                                }
                            },
                            enabled = adReadyState[AdLocation.EXTRA_SENSITIVITIES] == true
                        ) {
                            Text("Доп. настройки")
                        }
                    }
                }
            }
        }

        // Реклама при открытии приложения
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Реклама при открытии",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            activity?.let { adViewModel.showAppOpenAd(it) }
                        },
                        enabled = adReadyState[AdLocation.APP_STARTUP] == true
                    ) {
                        Text("Показать")
                    }
                }
            }
        }

        // Результат последнего показа
        item {
            lastAdResult?.let { result ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.success) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Последний результат",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Тип: ${result.adType}")
                        Text("Успех: ${if (result.success) "Да" else "Нет"}")
                        
                        result.reward?.let { reward ->
                            Text("Награда: ${reward.amount} ${reward.type}")
                        }
                        
                        result.error?.let { error ->
                            Text(
                                text = "Ошибка: ${error.message}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Пример использования в обычном экране приложения
 */
@Composable
fun RegularScreenWithAds(
    location: AdLocation = AdLocation.HOME_SCREEN,
    modifier: Modifier = Modifier,
    adViewModel: UnifiedAdViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Column(modifier = modifier) {
        // Основной контент экрана
        Box(modifier = Modifier.weight(1f)) {
            content()
        }

        // Баннерная реклама внизу
        AdBanner(location = location)
    }

    // Автоматический трекинг действий для интерстициальной рекламы
    LaunchedEffect(location) {
        // Симуляция действий пользователя
        repeat(5) {
            kotlinx.coroutines.delay(3000) // Ждем 3 секунды
            activity?.let { 
                adViewModel.trackActionAndShowInterstitial(location, it)
            }
        }
    }
}

/**
 * Хук для простого использования рекламы в Compose
 */
@Composable
fun rememberAdController(
    adViewModel: UnifiedAdViewModel = hiltViewModel()
): AdController {
    val context = LocalContext.current
    val activity = context as? Activity
    
    return remember(adViewModel, activity) {
        AdController(adViewModel, activity)
    }
}

/**
 * Контроллер для упрощения работы с рекламой
 */
class AdController(
    private val adViewModel: UnifiedAdViewModel,
    private val activity: Activity?
) {
    fun showInterstitial(location: AdLocation = AdLocation.HOME_SCREEN, onResult: (Boolean) -> Unit = {}) {
        activity?.let { 
            adViewModel.showInterstitialAd(location, it) { result ->
                onResult(result.success)
            }
        }
    }

    fun showRewarded(location: AdLocation = AdLocation.PREMIUM_FEATURES, onReward: () -> Unit = {}, onFailure: () -> Unit = {}) {
        activity?.let { 
            adViewModel.showRewardedAd(location, it) { result ->
                if (result.success && result.reward != null) {
                    onReward()
                } else {
                    onFailure()
                }
            }
        }
    }

    fun trackAction(location: AdLocation, onAdShown: () -> Unit = {}) {
        activity?.let { 
            adViewModel.trackActionAndShowInterstitial(location, it) { result ->
                if (result.success) {
                    onAdShown()
                }
            }
        }
    }

    fun showAppOpen(onResult: (Boolean) -> Unit = {}) {
        activity?.let { 
            adViewModel.showAppOpenAd(it) { result ->
                onResult(result.success)
            }
        }
    }
} 