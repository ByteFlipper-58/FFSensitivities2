package com.byteflipper.ffsensitivities.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.domain.usecase.FetchManufacturersUseCase
import com.byteflipper.ffsensitivities.domain.model.Manufacturer
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ManufacturerViewModel @Inject constructor(
    private val fetchManufacturers: FetchManufacturersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Manufacturer>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Manufacturer>>> = _uiState

    init {
        loadManufacturers()
    }

    private fun loadManufacturers() {
        viewModelScope.launch {
            try {
                val manufacturersState = fetchManufacturers()
                _uiState.value = manufacturersState
            } catch (e: Exception) {
                _uiState.value = UiState.NoInternet
            }
        }
    }

    fun retry() {
        _uiState.value = UiState.Loading
        loadManufacturers()
    }

    // Hilt используется для создания экземпляра, фабрика не требуется
}