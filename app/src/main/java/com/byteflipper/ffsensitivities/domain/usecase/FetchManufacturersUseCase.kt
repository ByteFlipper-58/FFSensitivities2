package com.byteflipper.ffsensitivities.domain.usecase

import com.byteflipper.ffsensitivities.data.repository.ManufacturerRepository
import com.byteflipper.ffsensitivities.domain.model.Manufacturer
import javax.inject.Inject

class FetchManufacturersUseCase @Inject constructor(
    private val manufacturerRepository: ManufacturerRepository
) {
    suspend operator fun invoke(): Result<List<Manufacturer>> {
        return manufacturerRepository.fetchManufacturers()
    }
}

