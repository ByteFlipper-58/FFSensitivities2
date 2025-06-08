package com.byteflipper.ffsensitivities.presentation.ui.screens

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.core.AdType
import com.byteflipper.ffsensitivities.ads.viewmodel.UnifiedAdViewModel
import com.byteflipper.ffsensitivities.ads.components.getDynamicBottomPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdTestScreen(
    navController: NavHostController,
    adViewModel: UnifiedAdViewModel = hiltViewModel()
) {
    val activity = LocalActivity.current as? Activity
    val adReadyState by adViewModel.adReadyState.collectAsState()
    val lastAdResult by adViewModel.lastAdResult.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🎯 Тест рекламы",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },

        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
}

@Composable
private fun AdStatusCard(adReadyState: Map<AdLocation, Boolean>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painterResource(id = R.drawable.dashboard_24px),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "📊 Статус готовности рекламы",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            adReadyState.forEach { (location, isReady) ->
                AdStatusRow(location = location, isReady = isReady)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AdStatusRow(location: AdLocation, isReady: Boolean) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isReady) 1f else 0.6f,
        animationSpec = tween(300)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isReady)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = getLocationDisplayName(location),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isReady) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Готова",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Icon(
                    painterResource(id = R.drawable.cancel_24px),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Не готова",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AdResultCard(result: com.byteflipper.ffsensitivities.ads.core.AdResult) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (result.success)
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                else
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (result.success) painterResource(id = R.drawable.check_circle_24px) else painterResource(id = R.drawable.error_24px),
                        contentDescription = null,
                        tint = if (result.success)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "📋 Последний результат",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                InfoChip(label = "Тип", value = result.adType.toString())
                Spacer(modifier = Modifier.height(8.dp))
                InfoChip(
                    label = "Статус",
                    value = if (result.success) "Успешно" else "Ошибка",
                    isSuccess = result.success
                )

                result.error?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoChip(
                        label = "Ошибка",
                        value = error.message ?: "Неизвестная ошибка",
                        isError = true
                    )
                }

                result.reward?.let { reward ->
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoChip(
                        label = "Награда",
                        value = "${reward.amount} ${reward.type}",
                        isReward = true
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String,
    isSuccess: Boolean = false,
    isError: Boolean = false,
    isReward: Boolean = false
) {
    val backgroundColor = when {
        isSuccess -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        isError -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        isReward -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, MaterialTheme.shapes.small)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = when {
                isSuccess -> MaterialTheme.colorScheme.primary
                isError -> MaterialTheme.colorScheme.error
                isReward -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun AppOpenAdSection(
    adViewModel: UnifiedAdViewModel,
    activity: Activity?,
    adReadyState: Map<AdLocation, Boolean>
) {
    AdSectionCard(
        title = "🚀 App Open реклама",
        description = "Реклама при запуске приложения",
        icon = painterResource(id = R.drawable.rocket_24px),
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
    ) {
        val isReady = adReadyState[AdLocation.APP_STARTUP] == true

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

@Composable
private fun InterstitialAdSection(
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

@Composable
private fun RewardedAdSection(
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

@Composable
private fun ActionTrackingSection(
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

@Composable
private fun FrequencyInfoCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.schedule_24px),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "⏱️ Частота показа рекламы",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val frequencies = listOf(
                "🏠 Главная: каждое 2-е действие" to painterResource(id = R.drawable.home_24px),
                "📱 Устройства: каждое 4-е действие" to painterResource(id = R.drawable.phone_android_24px),
                "⚙️ Настройки чувствительности: каждое 3-е действие" to painterResource(id = R.drawable.settings_24px),
                "🔧 Настройки: каждое 5-е действие" to painterResource(id = R.drawable.tune_24px)
            )

            frequencies.forEach { (text, icon) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            MaterialTheme.shapes.small
                        )
                        .padding(12.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AdSectionCard(
    title: String,
    description: String,
    icon: Painter,
    containerColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
private fun ModernAdButton(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isReady: Boolean = true
) {
    val buttonColors = if (isReady) {
        ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    ElevatedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        colors = buttonColors,
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = if (isReady) 6.dp else 2.dp
        )
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatusIndicator(
    isReady: Boolean,
    readyText: String,
    notReadyText: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isReady)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                MaterialTheme.shapes.small
            )
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(
                    if (isReady)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isReady) readyText else notReadyText,
            style = MaterialTheme.typography.bodySmall,
            color = if (isReady)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getLocationDisplayName(location: AdLocation): String {
    return when (location) {
        AdLocation.DEVICES_SCREEN -> "📱 Устройства"
        AdLocation.SENSITIVITIES_SCREEN -> "⚙️ Настройки чувствительности"
        AdLocation.HOME_SCREEN -> "🏠 Главная"
        AdLocation.SETTINGS_SCREEN -> "🔧 Настройки"
        AdLocation.MAIN_BANNER -> "🎯 Главный баннер"
        AdLocation.PREMIUM_FEATURES -> "💎 Премиум функции"
        AdLocation.EXTRA_SENSITIVITIES -> "⭐ Дополнительные настройки"
        AdLocation.APP_STARTUP -> "🚀 Запуск приложения"
    }
} 