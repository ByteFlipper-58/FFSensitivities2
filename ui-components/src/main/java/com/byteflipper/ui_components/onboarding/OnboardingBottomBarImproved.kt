package com.byteflipper.ui_components.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.byteflipper.ui_components.R

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
        
        // Индикаторы
        OnboardingIndicators(
            totalSteps = totalSteps,
            currentStep = currentStep,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Кнопки навигации
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Левая сторона - кнопка "Назад" или "Пропустить"
            Row {
                // Кнопка "Назад"
                AnimatedVisibility(
                    visible = currentStep > 0 && (currentOnboardingStep?.showBackButton != false),
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    OutlinedButton(
                        onClick = { manager.previousStep() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.onboarding_back))
                    }
                }
                
                // Кнопка "Пропустить"
                if (showSkipButton && currentOnboardingStep?.isSkippable == true && currentStep < totalSteps - 1) {
                    TextButton(
                        onClick = { manager.goToStep(totalSteps - 1) }
                    ) {
                        Text("Пропустить")
                    }
                }
            }
            
            // Правая сторона - кнопка "Далее" или "Завершить"
            if (currentStep == totalSteps - 1) {
                // Кнопка "Завершить"
                FilledIconButton(
                    onClick = onFinish,
                    enabled = canFinish,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(width = 120.dp, height = 48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Done,
                            contentDescription = stringResource(R.string.onboarding_finish),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.onboarding_finish),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (currentOnboardingStep?.showNextButton != false) {
                // Кнопка "Далее"
                FilledIconButton(
                    onClick = { manager.nextStep() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(width = 100.dp, height = 48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            stringResource(R.string.onboarding_next),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.onboarding_next),
                            modifier = Modifier.size(18.dp)
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
                text = "Шаг ${currentStep + 1} из $totalSteps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "${((currentStep + 1) * 100 / totalSteps)}%",
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