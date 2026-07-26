package com.byteflipper.ffsensitivities.data.repository

import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestPreferencesRepository @Inject constructor(
    private val dataStoreManager: DataStoreManager
) {
    fun getRequestSent(): Flow<Boolean> = dataStoreManager.getRequestSent()

    suspend fun setRequestSent(status: Boolean) {
        dataStoreManager.setRequestSent(status)
    }
}
