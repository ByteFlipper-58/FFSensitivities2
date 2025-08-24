package com.byteflipper.ffsensitivities.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.domain.usecase.FetchDevicesUseCase
import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val fetchDevicesUseCase: FetchDevicesUseCase
) : ViewModel() {

    private companion object {
        private const val TAG = "DeviceViewModel"
    }

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
            when (val result = fetchDevicesUseCase(manufacturer)) { // Use the correct parameter
                is UiState.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val devices = result.data as? List<DeviceModel> ?: emptyList()
                    // Filter out duplicates based on manufacturer and name
                    val uniqueDevices = devices.distinctBy { "${it.manufacturer}_${it.name}" }
                    _uiState.value = UiState.Success(uniqueDevices)
                }
                else -> {
                    // Pass through Loading, Error, NoInternet states directly
                    _uiState.value = result
                }
            }
        }
    }

    fun retry() {
        // Use lastManufacturer for retry
        lastManufacturer?.let { manu ->
             viewModelScope.launch { // Ensure retry is also within a coroutine scope
                 _uiState.value = UiState.Loading
                  when (val result = fetchDevicesUseCase(manu)) {
                     is UiState.Success<*> -> {
                         @Suppress("UNCHECKED_CAST")
                         val devices = result.data as? List<DeviceModel> ?: emptyList()
                         // Filter out duplicates based on manufacturer and name
                         val uniqueDevices = devices.distinctBy { "${it.manufacturer}_${it.name}" }
                         _uiState.value = UiState.Success(uniqueDevices)
                     }
                     else -> {
                         // Pass through Loading, Error, NoInternet states directly
                         _uiState.value = result
                     }
                 }
             }
        } ?: run {
            // Handle case where lastManufacturer is null (e.g., initial load failed before manufacturer was set)
             _uiState.value = UiState.Error("Cannot retry without a manufacturer.")
             Log.w(TAG, "Retry called but lastManufacturer is null.")
        }
    }
}
