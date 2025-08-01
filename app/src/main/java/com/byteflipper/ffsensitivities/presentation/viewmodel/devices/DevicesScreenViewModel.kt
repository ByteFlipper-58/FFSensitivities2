package com.byteflipper.ffsensitivities.presentation.viewmodel.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.data.repository.DevicesRepository
import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import com.byteflipper.ffsensitivities.presentation.ui.screens.devices.events.DevicesScreenEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DevicesScreenViewModel @Inject constructor(
    private val repository: DevicesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<DeviceModel>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<DeviceModel>>> = _uiState

    private val _navigationEvent = MutableStateFlow<DevicesScreenEvent?>(null)
    val navigationEvent: StateFlow<DevicesScreenEvent?> = _navigationEvent

    private var lastManufacturer: String? = null

    fun handleEvent(event: DevicesScreenEvent) {
        when (event) {
            is DevicesScreenEvent.NavigateBack -> {
                _navigationEvent.value = event
            }
            is DevicesScreenEvent.NavigateToSensitivities -> {
                _navigationEvent.value = event
            }
            is DevicesScreenEvent.RetryLoadDevices -> {
                retry()
            }
            is DevicesScreenEvent.TrackAdAction -> {
                // Трекинг рекламы будет обрабатываться в UI слое
            }
        }
    }

    fun fetchDevices(manufacturer: String) {
        if (manufacturer == lastManufacturer && _uiState.value !is UiState.Loading) {
            return
        }
        lastManufacturer = manufacturer
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = repository.fetchDevices(manufacturer)) {
                is UiState.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val devices = result.data as? List<DeviceModel> ?: emptyList()
                    val uniqueDevices = devices.distinctBy { "${it.manufacturer}_${it.name}" }
                    _uiState.value = UiState.Success(uniqueDevices)
                }
                else -> {
                    _uiState.value = result
                }
            }
        }
    }

    private fun retry() {
        lastManufacturer?.let { manufacturer ->
            viewModelScope.launch {
                _uiState.value = UiState.Loading
                when (val result = repository.fetchDevices(manufacturer)) {
                    is UiState.Success<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val devices = result.data as? List<DeviceModel> ?: emptyList()
                        val uniqueDevices = devices.distinctBy { "${it.manufacturer}_${it.name}" }
                        _uiState.value = UiState.Success(uniqueDevices)
                    }
                    else -> {
                        _uiState.value = result
                    }
                }
            }
        } ?: run {
            _uiState.value = UiState.Error("Cannot retry without a manufacturer.")
        }
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
} 