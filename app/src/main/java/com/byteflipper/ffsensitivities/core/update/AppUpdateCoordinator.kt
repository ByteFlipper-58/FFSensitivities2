package com.byteflipper.ffsensitivities.core.update

import com.byteflipper.ffsensitivities.playcore.UpdateState
import kotlinx.coroutines.flow.StateFlow

interface AppUpdateCoordinator {
    val updateState: StateFlow<UpdateState>

    suspend fun checkForUpdate()
    fun startUpdate()
    fun completeUpdate()
    fun resumeUpdate()
}
