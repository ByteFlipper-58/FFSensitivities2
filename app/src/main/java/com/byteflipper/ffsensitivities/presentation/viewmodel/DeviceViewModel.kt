package com.byteflipper.ffsensitivities.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.data.repository.DevicesRepository
import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val repository: DevicesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<DeviceModel>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<DeviceModel>>> = _uiState

    private var lastManufacturer: String? = null // Renamed from lastModel

    fun fetchDevices(manufacturer: String) { // Renamed parameter from model
        // Avoid refetching if the manufacturer hasn't changed
        if (manufacturer == lastManufacturer && _uiState.value !is UiState.Loading) {
             // Optionally check if it's already Success to avoid unnecessary loading state flicker
             // if (_uiState.value is UiState.Success<*>) return
        }
        lastManufacturer = manufacturer // Store the last fetched manufacturer
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = repository.fetchDevices(manufacturer) // Use the correct parameter
        }
    }

    fun retry() {
        // Use lastManufacturer for retry
        lastManufacturer?.let { manu ->
             viewModelScope.launch { // Ensure retry is also within a coroutine scope
                 _uiState.value = UiState.Loading
                 _uiState.value = repository.fetchDevices(manu)
             }
        } ?: run {
            // Handle case where lastManufacturer is null (e.g., initial load failed before manufacturer was set)
             _uiState.value = UiState.Error("Cannot retry without a manufacturer.")
             Timber.tag("DeviceViewModel").w("Retry called but lastManufacturer is null.")
        }
    }
}
