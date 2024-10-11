package com.byteflipper.ffsensitivities.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.byteflipper.ffsensitivities.data.Manufacturer
import com.byteflipper.ffsensitivities.service.RetrofitInstance

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class ManufacturerViewModel : ViewModel() {
    private val _uiState = mutableStateOf<UiState<List<Manufacturer>>>(UiState.Loading)
    val uiState: State<UiState<List<Manufacturer>>> = _uiState

    init {
        fetchManufacturers()
    }

    private fun fetchManufacturers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = RetrofitInstance.api.getManufacturers()
                _uiState.value = UiState.Success(response.manufacturers)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Ошибка загрузки данных: ${e.message}")
            }
        }
    }
}
