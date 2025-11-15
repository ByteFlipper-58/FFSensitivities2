package com.byteflipper.ffsensitivities.presentation.viewmodel.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import com.byteflipper.ffsensitivities.domain.usecase.FetchDevicesUseCase
import com.byteflipper.ffsensitivities.presentation.devices.events.DevicesScreenEvent
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DevicesScreenViewModel @Inject constructor(
    private val fetchDevicesUseCase: FetchDevicesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<DeviceModel>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<DeviceModel>>> = _uiState

    private val _navigationEvent = MutableStateFlow<DevicesScreenEvent?>(null)
    val navigationEvent: StateFlow<DevicesScreenEvent?> = _navigationEvent

    private var lastManufacturer: String? = null

    fun handleEvent(event: DevicesScreenEvent) {
        when (event) {
            is DevicesScreenEvent.NavigateBack -> _navigationEvent.value = event
            is DevicesScreenEvent.NavigateToSensitivities -> _navigationEvent.value = event
            is DevicesScreenEvent.RetryLoadDevices -> retry()
            is DevicesScreenEvent.TrackAdAction -> Unit
        }
    }

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

    private fun retry() {
        lastManufacturer?.let { fetchDevices(it) }
            ?: run { _uiState.value = UiState.Error("Cannot retry without a manufacturer.") }
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }

    private fun Throwable.toUiStateError(): UiState<List<DeviceModel>> {
        return when (this) {
            is IOException, is UnknownHostException -> UiState.NoInternet
            else -> UiState.Error(message ?: "Unexpected error")
        }
    }
}
