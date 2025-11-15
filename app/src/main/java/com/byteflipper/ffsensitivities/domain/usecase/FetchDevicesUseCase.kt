package com.byteflipper.ffsensitivities.domain.usecase

import com.byteflipper.ffsensitivities.data.repository.DevicesRepository
import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import javax.inject.Inject

class FetchDevicesUseCase @Inject constructor(
    private val devicesRepository: DevicesRepository
) {
    suspend operator fun invoke(manufacturer: String): Result<List<DeviceModel>> {
        return devicesRepository.fetchDevices(manufacturer)
    }
}

