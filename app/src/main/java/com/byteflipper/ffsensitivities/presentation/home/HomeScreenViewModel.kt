package com.byteflipper.ffsensitivities.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.data.repository.RequestPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val requestPreferencesRepository: RequestPreferencesRepository
) : ViewModel() {

    // Преобразуем Flow в StateFlow для удобного использования в Compose
    val isRequestSent = requestPreferencesRepository.getRequestSent()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Функция для сохранения статуса отправки запроса (для обратной совместимости)
    fun setRequestSent(status: Boolean) {
        viewModelScope.launch {
            requestPreferencesRepository.setRequestSent(status)
        }
    }
}
