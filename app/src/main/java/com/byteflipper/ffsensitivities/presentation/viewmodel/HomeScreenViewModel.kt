package com.byteflipper.ffsensitivities.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    // Преобразуем Flow в StateFlow для удобного использования в Compose
    val isRequestSent = dataStoreManager.getRequestSent()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Начинаем сбор при наличии подписчиков
            initialValue = false // Начальное значение по умолчанию
        )

    // Функция для сохранения статуса отправки запроса
    fun setRequestSent(status: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setRequestSent(status)
        }
    }
}
