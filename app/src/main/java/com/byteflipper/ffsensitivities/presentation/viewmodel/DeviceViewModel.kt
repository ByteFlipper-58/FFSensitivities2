package com.byteflipper.ffsensitivities.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import com.byteflipper.ffsensitivities.domain.usecase.FetchDevicesUseCase
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val fetchDevicesUseCase: FetchDevicesUseCase
) : ViewModel() {

    private companion object {
        private const val TAG = "DeviceViewModel"
    }

    private val _uiState = MutableStateFlow<UiState<List<DeviceModel>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<DeviceModel>>> = _uiState

    private var lastManufacturer: String? = null

    fun fetchDevices(manufacturer: String) {
        if (manufacturer == lastManufacturer && _uiState.value is UiState.Success<*>) return
        lastManufacturer = manufacturer
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            fetchDevicesUseCase(manufacturer)
                .onSuccess { devices -> _uiState.value = UiState.Success(devices) }
                .onFailure { error -> _uiState.value = error.toUiStateError() }
        }
    }

    fun retry() {
        lastManufacturer?.let { fetchDevices(it) } ?: run {
            _uiState.value = UiState.Error("Cannot retry without a manufacturer.")
            Log.w(TAG, "Retry called but lastManufacturer is null.")
        }
    }

    private fun Throwable.toUiStateError(): UiState<List<DeviceModel>> {
        return when (this) {
            is IOException, is UnknownHostException -> UiState.NoInternet
            else -> UiState.Error(message ?: "Unexpected error")
        }
    }
}
