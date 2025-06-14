package com.byteflipper.ui_components.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

/**
 * Централизованный менеджер для управления OnBoarding flow
 * Обеспечивает единое место для:
 * - Управления состоянием онбординга
 * - Настройки страниц и их последовательности
 * - Обработки условий завершения
 * - Сохранения прогресса
 */
class OnboardingManager {
    
    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()
    
    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()
    
    private val _canFinish = MutableStateFlow(false)
    val canFinish: StateFlow<Boolean> = _canFinish.asStateFlow()
    
    private var _steps: List<OnboardingStep> = emptyList()
    val steps: List<OnboardingStep> get() = _steps
    
    private var _finishConditions: List<OnboardingCondition> = emptyList()
    
    /**
     * Настройка шагов онбординга
     */
    fun setupSteps(steps: List<OnboardingStep>) {
        _steps = steps
        _currentStep.value = 0
        updateFinishCondition()
    }
    
    /**
     * Установка условий для завершения онбординга
     */
    fun setFinishConditions(conditions: List<OnboardingCondition>) {
        _finishConditions = conditions
        updateFinishCondition()
    }
    
    /**
     * Переход к следующему шагу
     */
    fun nextStep() {
        if (_currentStep.value < _steps.size - 1) {
            _currentStep.value++
            updateFinishCondition()
        }
    }
    
    /**
     * Переход к предыдущему шагу
     */
    fun previousStep() {
        if (_currentStep.value > 0) {
            _currentStep.value--
            updateFinishCondition()
        }
    }
    
    /**
     * Переход к конкретному шагу
     */
    fun goToStep(step: Int) {
        if (step in 0 until _steps.size) {
            _currentStep.value = step
            updateFinishCondition()
        }
    }
    
    /**
     * Завершение онбординга
     */
    suspend fun finish(onComplete: suspend () -> Unit) {
        Log.d("OnboardingManager", "finish() вызван, canFinish=${_canFinish.value}")
        if (_canFinish.value) {
            Log.d("OnboardingManager", "Условия выполнены, завершаем OnBoarding")
            _isCompleted.value = true
            onComplete()
        } else {
            Log.w("OnboardingManager", "Условия НЕ выполнены, завершение отменено")
        }
    }
    
    /**
     * Сброс состояния онбординга
     */
    fun reset() {
        _currentStep.value = 0
        _isCompleted.value = false
        _canFinish.value = false
    }
    
    /**
     * Обновление условия завершения
     */
    private fun updateFinishCondition() {
        val isLastStep = _currentStep.value == _steps.size - 1
        
        if (isLastStep) {
            // На последнем шаге проверяем только условия, которые должны быть выполнены для завершения
            val relevantConditions = _finishConditions.filter { condition ->
                // Для последнего шага проверяем только условие принятия условий
                condition.id == "terms_accepted"
            }
            val allConditionsMet = relevantConditions.all { it.isMet.value }
            
            Log.d("OnboardingManager", "updateFinishCondition: isLastStep=$isLastStep")
            relevantConditions.forEach { condition ->
                Log.d("OnboardingManager", "Условие ${condition.id}: ${condition.isMet.value}")
            }
            Log.d("OnboardingManager", "canFinish устанавливается в: $allConditionsMet")
            
            _canFinish.value = allConditionsMet
        } else {
            _canFinish.value = false
        }
    }
    
    /**
     * Обновление условия (например, при изменении разрешений)
     */
    fun updateCondition(conditionId: String, isMet: Boolean) {
        Log.d("OnboardingManager", "updateCondition: conditionId=$conditionId, isMet=$isMet")
        _finishConditions.find { it.id == conditionId }?.let { condition ->
            Log.d("OnboardingManager", "Условие найдено, старое значение: ${condition.isMet.value}")
            condition.isMet.value = isMet
            Log.d("OnboardingManager", "Условие обновлено на: ${condition.isMet.value}")
            updateFinishCondition()
        } ?: run {
            Log.w("OnboardingManager", "Условие с id=$conditionId не найдено")
        }
    }
    
    /**
     * Принудительное обновление условий завершения
     */
    fun forceUpdateFinishCondition() {
        updateFinishCondition()
    }
}

/**
 * Шаг онбординга с дополнительными возможностями
 */
data class OnboardingStep(
    val id: String,
    val title: String? = null,
    val isSkippable: Boolean = true,
    val showBackButton: Boolean = true,
    val showNextButton: Boolean = true,
    val customActions: List<OnboardingAction> = emptyList(),
    val content: @Composable OnboardingStepScope.() -> Unit
)

/**
 * Scope для шага онбординга, предоставляющий доступ к менеджеру
 */
interface OnboardingStepScope {
    val manager: OnboardingManager
    val stepIndex: Int
    val totalSteps: Int
    val isLastStep: Boolean
    val isFirstStep: Boolean
}

/**
 * Условие для завершения онбординга
 */
data class OnboardingCondition(
    val id: String,
    val description: String,
    val isMet: MutableStateFlow<Boolean> = MutableStateFlow(false)
)

/**
 * Кастомное действие для шага
 */
data class OnboardingAction(
    val id: String,
    val label: String,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)

/**
 * Composable функция для создания менеджера онбординга
 */
@Composable
fun rememberOnboardingManager(): OnboardingManager {
    return remember { OnboardingManager() }
}

/**
 * Hook для создания условий онбординга
 */
@Composable
fun rememberOnboardingConditions(
    vararg conditions: Pair<String, String>
): List<OnboardingCondition> {
    return remember {
        conditions.map { (id, description) ->
            OnboardingCondition(id, description)
        }
    }
}

/**
 * Extension для подписки на состояние условия
 */
@Composable
fun OnboardingCondition.collectAsState(): State<Boolean> {
    return isMet.collectAsState()
} 