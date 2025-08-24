package com.byteflipper.ffsensitivities.domain.usecase

import com.byteflipper.ffsensitivities.data.repository.DevicesRepository
import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import javax.inject.Inject

class FetchDevicesUseCase @Inject constructor(
    private val devicesRepository: DevicesRepository
) {
    suspend operator fun invoke(manufacturer: String): UiState<List<DeviceModel>> {
        return devicesRepository.fetchDevices(manufacturer)
    }
}

