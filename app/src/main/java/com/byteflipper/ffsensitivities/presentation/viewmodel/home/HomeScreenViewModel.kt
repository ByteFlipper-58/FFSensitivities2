package com.byteflipper.ffsensitivities.presentation.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import com.byteflipper.ffsensitivities.presentation.ui.screens.home.events.HomeScreenEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog

    // Преобразуем Flow в StateFlow для удобного использования в Compose
    val isRequestSent = dataStoreManager.getRequestSent()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun handleEvent(event: HomeScreenEvent) {
        when (event) {
            is HomeScreenEvent.ShowRequestDialog -> {
                _showDialog.value = true
            }
            is HomeScreenEvent.HideRequestDialog -> {
                _showDialog.value = false
            }
            is HomeScreenEvent.SendSensitivitiesRequest -> {
                viewModelScope.launch {
                    dataStoreManager.setRequestSent(true)
                }
                _showDialog.value = false
            }
            is HomeScreenEvent.NavigateToSettings -> {
                // Навигация будет обрабатываться в UI слое
            }
            is HomeScreenEvent.NavigateToDevices -> {
                // Навигация будет обрабатываться в UI слое
            }
            is HomeScreenEvent.RetryLoadManufacturers -> {
                // Перезагрузка будет обрабатываться в ManufacturerViewModel
            }
            is HomeScreenEvent.TrackAdAction -> {
                // Трекинг рекламы будет обрабатываться в UI слое
            }
        }
    }

    // Функция для сохранения статуса отправки запроса (для обратной совместимости)
    fun setRequestSent(status: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setRequestSent(status)
        }
    }
} 