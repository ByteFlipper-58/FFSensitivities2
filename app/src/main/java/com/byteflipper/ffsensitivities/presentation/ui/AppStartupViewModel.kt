package com.byteflipper.ffsensitivities.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.data.repository.OnboardingRepository
import com.byteflipper.ffsensitivities.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class AppStartupViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    private val _startRoute = MutableStateFlow<String?>(null)
    val startRoute: StateFlow<String?> = _startRoute.asStateFlow()

    init {
        resolveStartRoute()
    }

    fun resolveStartRoute() {
        if (_startRoute.value != null) return

        viewModelScope.launch {
            _startRoute.value = if (onboardingRepository.isFirstLaunchCompleted().first()) {
                Route.MAIN_APP
            } else {
                Route.ONBOARDING
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingRepository.setFirstLaunchCompleted(true)
            _startRoute.value = Route.MAIN_APP
        }
    }
}
