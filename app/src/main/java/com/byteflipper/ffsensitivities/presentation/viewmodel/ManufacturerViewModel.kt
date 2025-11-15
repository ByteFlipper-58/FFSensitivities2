package com.byteflipper.ffsensitivities.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.domain.model.Manufacturer
import com.byteflipper.ffsensitivities.domain.usecase.FetchManufacturersUseCase
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import java.net.UnknownHostException
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
            _uiState.value = UiState.Loading
            fetchManufacturers()
                .onSuccess { manufacturers -> _uiState.value = UiState.Success(manufacturers) }
                .onFailure { error -> _uiState.value = error.toUiStateError() }
        }
    }

    fun retry() {
        loadManufacturers()
    }

    private fun Throwable.toUiStateError(): UiState<List<Manufacturer>> {
        return when (this) {
            is IOException, is UnknownHostException -> UiState.NoInternet
            else -> UiState.Error(message ?: "Unexpected error")
        }
    }
}
