package com.byteflipper.ffsensitivities.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.data.DeviceModel
import com.byteflipper.ffsensitivities.service.DevicesRetrofitInstance
import com.byteflipper.ffsensitivities.ui.UiState
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class DeviceViewModel : ViewModel() {
    private val _uiState = mutableStateOf<UiState<List<DeviceModel>>>(UiState.Loading)
    val uiState: State<UiState<List<DeviceModel>>> = _uiState

    private var lastModel: String? = null

    fun fetchDevices(model: String) {
        lastModel = model
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = DevicesRetrofitInstance.api.getDevicesForModel(model)
                _uiState.value = UiState.Success(response.models)
            } catch (e: UnknownHostException) {
                _uiState.value = UiState.NoInternet
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Ошибка загрузки данных: ${e.message}")
            }
        }
    }

    fun retry() {
        lastModel?.let { fetchDevices(it) }
    }
}