package com.byteflipper.ffsensitivities.domain.usecase

import com.byteflipper.ffsensitivities.data.repository.ManufacturerRepository
import com.byteflipper.ffsensitivities.domain.model.Manufacturer
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import javax.inject.Inject

class FetchManufacturersUseCase @Inject constructor(
    private val manufacturerRepository: ManufacturerRepository
) {
    suspend operator fun invoke(): UiState<List<Manufacturer>> {
        return manufacturerRepository.fetchManufacturers()
    }
}

