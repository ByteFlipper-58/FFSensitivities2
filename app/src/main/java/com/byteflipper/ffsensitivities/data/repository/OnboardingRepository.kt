package com.byteflipper.ffsensitivities.data.repository

import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingRepository @Inject constructor(
    private val dataStoreManager: DataStoreManager
) {
    fun isFirstLaunchCompleted(): Flow<Boolean> = dataStoreManager.getFirstLaunchCompleted()

    suspend fun setFirstLaunchCompleted(completed: Boolean) {
        dataStoreManager.setFirstLaunchCompleted(completed)
    }
}
