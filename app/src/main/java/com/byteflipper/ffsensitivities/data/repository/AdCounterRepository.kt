package com.byteflipper.ffsensitivities.data.repository

import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdCounterRepository @Inject constructor(
    private val dataStoreManager: DataStoreManager
) {
    suspend fun incrementCounter(location: String): Int {
        return dataStoreManager.incrementAdCounter(location)
    }

    suspend fun resetCounter(location: String) {
        dataStoreManager.resetAdCounter(location)
    }
}
