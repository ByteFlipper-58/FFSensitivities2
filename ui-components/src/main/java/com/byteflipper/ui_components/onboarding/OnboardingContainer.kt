package com.byteflipper.ui_components.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.byteflipper.ui_components.R
import kotlinx.coroutines.launch

/**
 * Улучшенный контейнер для OnBoarding с централизованным управлением
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingContainer(
    manager: OnboardingManager,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    enableSwipeGestures: Boolean = true,
    animationDurationMs: Int = 300,
    onComplete: suspend () -> Unit = {}
) {
    val currentStep by manager.currentStep.collectAsState()
    val canFinish by manager.canFinish.collectAsState()
    val steps = manager.steps
    val coroutineScope = rememberCoroutineScope()
    
    // Используем PagerState только если включены жесты свайпа
    val pagerState = if (enableSwipeGestures) {
        rememberPagerState(pageCount = { steps.size })
    } else null
    
    // Синхронизация менеджера с pager state
    LaunchedEffect(pagerState) {
        pagerState?.let { state ->
            snapshotFlow { state.currentPage }.collect { page ->
                if (page != currentStep) {
                    manager.goToStep(page)
                }
            }
        }
    }
    
    // Синхронизация pager state с менеджером
    LaunchedEffect(currentStep) {
        pagerState?.let { state ->
            if (state.currentPage != currentStep) {
                state.animateScrollToPage(currentStep)
            }
        }
    }
    
    if (steps.isEmpty()) {
        return
    }
    
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                if (showTitle && steps[currentStep].title != null) {
                    OnboardingTopBar(
                        title = steps[currentStep].title!!,
                        currentStep = currentStep + 1,
                        totalSteps = steps.size
                    )
                }
            },
            bottomBar = {
                OnboardingBottomBarImproved(
                    manager = manager,
                    currentStep = currentStep,
                    totalSteps = steps.size,
                    canFinish = canFinish,
                    onFinish = {
                        coroutineScope.launch {
                            manager.finish(onComplete)
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (enableSwipeGestures && pagerState != null) {
                // Режим с поддержкой свайпов
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) { pageIndex ->
                    OnboardingStepWrapper(
                        step = steps[pageIndex],
                        manager = manager,
                        stepIndex = pageIndex,
                        totalSteps = steps.size
                    )
                }
            } else {
                // Режим только с кнопками навигации
                AnimatedContent(
                    targetState = currentStep,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    transitionSpec = {
                        val direction = if (targetState > initialState) 1 else -1
                        
                        slideInHorizontally(
                            animationSpec = tween(animationDurationMs, easing = EaseInOut),
                            initialOffsetX = { it * direction }
                        ) + fadeIn(
                            animationSpec = tween(animationDurationMs)
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(animationDurationMs, easing = EaseInOut),
                            targetOffsetX = { -it * direction }
                        ) + fadeOut(
                            animationSpec = tween(animationDurationMs)
                        )
                    },
                    label = "OnboardingStepTransition"
                ) { stepIndex ->
                    OnboardingStepWrapper(
                        step = steps[stepIndex],
                        manager = manager,
                        stepIndex = stepIndex,
                        totalSteps = steps.size
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingTopBar(
    title: String,
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
                    Text(
                text = stringResource(R.string.onboarding_step_indicator, currentStep, totalSteps),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun OnboardingStepWrapper(
    step: OnboardingStep,
    manager: OnboardingManager,
    stepIndex: Int,
    totalSteps: Int
) {
    val scope = object : OnboardingStepScope {
        override val manager: OnboardingManager = manager
        override val stepIndex: Int = stepIndex
        override val totalSteps: Int = totalSteps
        override val isLastStep: Boolean = stepIndex == totalSteps - 1
        override val isFirstStep: Boolean = stepIndex == 0
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        step.content(scope)
    }
}

/**
 * Простой способ создания OnBoarding с базовыми настройками
 */
@Composable
fun SimpleOnboarding(
    steps: List<OnboardingStep>,
    conditions: List<OnboardingCondition> = emptyList(),
    modifier: Modifier = Modifier,
    onComplete: suspend () -> Unit = {}
) {
    val manager = rememberOnboardingManager()
    
    LaunchedEffect(steps, conditions) {
        manager.setupSteps(steps)
        manager.setFinishConditions(conditions)
    }
    
    OnboardingContainer(
        manager = manager,
        modifier = modifier,
        onComplete = onComplete
    )
}

/**
 * Builder для создания шагов OnBoarding
 */
class OnboardingStepsBuilder {
    private val steps = mutableListOf<OnboardingStep>()
    
    fun step(
        id: String,
        title: String? = null,
        isSkippable: Boolean = true,
        showBackButton: Boolean = true,
        showNextButton: Boolean = true,
        content: @Composable OnboardingStepScope.() -> Unit
    ) {
        steps.add(
            OnboardingStep(
                id = id,
                title = title,
                isSkippable = isSkippable,
                showBackButton = showBackButton,
                showNextButton = showNextButton,
                content = content
            )
        )
    }
    
    internal fun build(): List<OnboardingStep> = steps.toList()
}

/**
 * DSL для создания шагов OnBoarding
 */
fun buildOnboardingSteps(builder: OnboardingStepsBuilder.() -> Unit): List<OnboardingStep> {
    return OnboardingStepsBuilder().apply(builder).build()
} 