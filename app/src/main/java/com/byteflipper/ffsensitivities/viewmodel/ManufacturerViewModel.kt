package com.byteflipper.ffsensitivities.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.data.Manufacturer
import com.byteflipper.ffsensitivities.repository.ManufacturerRepository
import com.byteflipper.ffsensitivities.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ManufacturerViewModel(
    private val repository: ManufacturerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Manufacturer>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Manufacturer>>> = _uiState

    init {
        loadManufacturers()
    }

    private fun loadManufacturers() {
        viewModelScope.launch {
            try {
                val manufacturersState = repository.fetchManufacturers()
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

    class Factory(private val repository: ManufacturerRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ManufacturerViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ManufacturerViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}