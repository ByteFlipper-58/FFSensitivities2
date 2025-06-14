package com.byteflipper.ui_components.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.byteflipper.ui_components.R
import android.util.Log

/**
 * Улучшенная нижняя панель навигации для OnBoarding с дополнительными возможностями
 */
@Composable
fun OnboardingBottomBarImproved(
    manager: OnboardingManager,
    currentStep: Int,
    totalSteps: Int,
    canFinish: Boolean,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    showProgress: Boolean = true,
    showSkipButton: Boolean = true
) {
    val currentOnboardingStep = if (manager.steps.isNotEmpty() && currentStep < manager.steps.size) {
        manager.steps[currentStep]
    } else null
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // Прогресс бар
        if (showProgress) {
            OnboardingProgressBar(
                currentStep = currentStep,
                totalSteps = totalSteps,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Индикаторы шагов с красивой анимацией
        OnboardingIndicators(
            totalSteps = totalSteps,
            currentStep = currentStep,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Кнопки навигации в правом нижнем углу
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Контейнер для обеих кнопок с фиксированным размером
            Row(
                modifier = Modifier.width(124.dp), // Фиксированная ширина: 56 + 12 + 56
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка "Назад" с улучшенной анимацией
                AnimatedVisibility(
                    visible = currentStep > 0 && (currentOnboardingStep?.showBackButton != false),
                    enter = slideInHorizontally(
                        initialOffsetX = { -it / 2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessVeryLow
                        )
                    ) + fadeIn(
                        animationSpec = tween(500, delayMillis = 100)
                    ) + scaleIn(
                        initialScale = 0.7f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessVeryLow
                        )
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { -it / 2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeOut(
                        animationSpec = tween(250)
                    ) + scaleOut(
                        targetScale = 0.8f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                ) {
                    FloatingActionButton(
                        onClick = { manager.previousStep() },
                        modifier = Modifier.size(56.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.onboarding_back),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Отступ между кнопками
                if (currentStep > 0 && (currentOnboardingStep?.showBackButton != false)) {
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                // Кнопка "Далее" или "Завершить" - всегда справа
                if (currentStep == totalSteps - 1) {
                    // Кнопка "Завершить"
                    FloatingActionButton(
                        onClick = {
                            Log.d("OnboardingBottomBar", "Клик на кнопку Done: canFinish=$canFinish")
                            if (canFinish) {
                                Log.d("OnboardingBottomBar", "Вызываем onFinish()")
                                onFinish()
                            } else {
                                Log.d("OnboardingBottomBar", "canFinish=false, не вызываем onFinish()")
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = if (canFinish) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (canFinish) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Icon(
                            Icons.Default.Done,
                            contentDescription = stringResource(R.string.onboarding_finish),
                            modifier = Modifier.size(24.dp),
                            tint = if (canFinish) 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else if (currentOnboardingStep?.showNextButton != false) {
                    // Кнопка "Далее"
                    FloatingActionButton(
                        onClick = { manager.nextStep() },
                        modifier = Modifier.size(56.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.onboarding_next),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        
        // Кастомные действия шага
        currentOnboardingStep?.customActions?.let { actions ->
            if (actions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    actions.forEach { action ->
                        TextButton(
                            onClick = action.onClick,
                            enabled = action.enabled,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(action.label)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.onboarding_step_indicator, currentStep + 1, totalSteps),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = stringResource(R.string.onboarding_progress_percent, (currentStep + 1) * 100 / totalSteps),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = { (currentStep + 1).toFloat() / totalSteps },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun OnboardingIndicators(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            OnboardingIndicator(
                isSelected = index == currentStep,
                isCompleted = index < currentStep,
                onClick = { /* TODO: Можно добавить переход по клику */ }
            )
            if (index < totalSteps - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun OnboardingIndicator(
    isSelected: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val springSpec = spring<Dp>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val colorSpringSpec = spring<Color>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val width by animateDpAsState(
        targetValue = when {
            isSelected -> 32.dp
            isCompleted -> 12.dp
            else -> 8.dp
        },
        animationSpec = springSpec,
        label = "IndicatorWidth"
    )
    
    val height by animateDpAsState(
        targetValue = when {
            isSelected -> 8.dp
            isCompleted -> 12.dp
            else -> 8.dp
        },
        animationSpec = springSpec,
        label = "IndicatorHeight"
    )
    
    val color by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isCompleted -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        },
        animationSpec = colorSpringSpec,
        label = "IndicatorColor"
    )
    
    Box(
        modifier = modifier
            .height(height)
            .width(width)
            .clip(if (isCompleted) CircleShape else RoundedCornerShape(4.dp))
            .background(color)
            .clickable { onClick() }
    ) {
        if (isCompleted) {
            Icon(
                Icons.Default.Done,
                contentDescription = null,
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.Center),
                tint = MaterialTheme.colorScheme.surface
            )
        }
    }
} 