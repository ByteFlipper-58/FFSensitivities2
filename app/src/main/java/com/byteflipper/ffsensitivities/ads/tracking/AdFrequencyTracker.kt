package com.byteflipper.ffsensitivities.ads.tracking

import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encapsulates interaction with DataStore for ad frequency counters.
 */
@Singleton
class AdFrequencyTracker @Inject constructor(
    private val dataStoreManager: DataStoreManager
) {

    suspend fun increment(location: AdLocation): Int {
        return key(location)?.let { dataStoreManager.incrementAdCounter(it) } ?: 0
    }

    suspend fun reset(location: AdLocation) {
        key(location)?.let { dataStoreManager.resetAdCounter(it) }
    }

    private fun key(location: AdLocation): String? {
        return when (location) {
            AdLocation.HOME_SCREEN -> "HOME_SCREEN"
            AdLocation.DEVICES_SCREEN -> "DEVICES_SCREEN"
            AdLocation.SENSITIVITIES_SCREEN -> "SENSITIVITIES_SCREEN"
            AdLocation.SETTINGS_SCREEN -> "SETTINGS_SCREEN"
            else -> null
        }
    }
}
