package com.byteflipper.ui_components.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byteflipper.ui_components.R

/**
 * Пример использования улучшенного OnBoarding с новой архитектурой
 */
@Composable
fun OnboardingExample(
    logoPainter: Painter,
    onComplete: suspend () -> Unit
) {
    // Состояния для условий завершения
    var hasNotificationPermission by remember { mutableStateOf(false) }
    var isTermsAccepted by remember { mutableStateOf(false) }
    
    // Создание условий
    val conditions = rememberOnboardingConditions(
        "notification_permission" to "Разрешение на уведомления",
        "terms_accepted" to "Принятие условий"
    )
    
    // Обновление условий при изменении состояний
    if (conditions.isNotEmpty()) {
        conditions[0].isMet.value = hasNotificationPermission
        conditions[1].isMet.value = isTermsAccepted
    }
    
    // Создание шагов с помощью DSL
    val steps = buildOnboardingSteps {
        step(
            id = "welcome",
            title = "Добро пожаловать!",
            isSkippable = false
        ) {
            WelcomeStepContent(
                logoPainter = logoPainter,
                title = "Добро пожаловать в FF Sensitivities!",
                description = "Найдите лучшие настройки чувствительности для вашего устройства и улучшите свою игру."
            )
        }
        
        step(
            id = "permissions",
            title = "Разрешения",
            isSkippable = true
        ) {
            PermissionsStepContent(
                hasPermission = hasNotificationPermission,
                onGrantPermission = { hasNotificationPermission = true }
            )
        }
        
        step(
            id = "terms",
            title = "Условия использования",
            isSkippable = false
        ) {
            TermsStepContent(
                isAccepted = isTermsAccepted,
                onAcceptTerms = { isTermsAccepted = it }
            )
        }
    }
    
    // Использование SimpleOnboarding
    SimpleOnboarding(
        steps = steps,
        conditions = conditions,
        onComplete = onComplete
    )
}

@Composable
private fun WelcomeStepContent(
    logoPainter: Painter,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = logoPainter,
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun PermissionsStepContent(
    hasPermission: Boolean,
    onGrantPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
                        Text(
                    text = stringResource(R.string.onboarding_permission_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
                        Text(
                    text = stringResource(R.string.onboarding_permission_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onGrantPermission,
            enabled = !hasPermission
        ) {
            Text(if (hasPermission) "Разрешение предоставлено" else "Предоставить разрешение")
        }
    }
}

@Composable
private fun TermsStepContent(
    isAccepted: Boolean,
    onAcceptTerms: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
                        Text(
                    text = stringResource(R.string.onboarding_terms_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
                        Text(
                    text = stringResource(R.string.onboarding_terms_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { onAcceptTerms(!isAccepted) }
        ) {
            Text(if (isAccepted) "Условия приняты" else "Принять условия")
        }
    }
}

/**
 * Пример продвинутого использования с кастомной настройкой
 */
@Composable
fun AdvancedOnboardingExample(
    onComplete: suspend () -> Unit
) {
    val manager = rememberOnboardingManager()
    
    // Настройка с кастомными опциями
    OnboardingContainer(
        manager = manager,
        showTitle = true,
        enableSwipeGestures = false, // Отключить свайпы
        animationDurationMs = 500,    // Медленная анимация
        onComplete = onComplete
    )
} 