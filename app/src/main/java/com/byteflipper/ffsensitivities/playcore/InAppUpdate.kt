package com.byteflipper.ffsensitivities.playcore

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.byteflipper.ffsensitivities.BuildConfig
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

enum class UpdateState {
    CHECKING, AVAILABLE, DOWNLOADING, DOWNLOADED, INSTALLING, INSTALLED, FAILED
}

class AppUpdateManagerWrapper(private val activity: ComponentActivity) : DefaultLifecycleObserver {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private var updateInfo: AppUpdateInfo? = null
    private var updateType: Int = AppUpdateType.FLEXIBLE
    private val _updateState = MutableStateFlow(UpdateState.CHECKING)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private var updateDialogLauncher: ActivityResultLauncher<Intent>? = null
    private val installStateUpdatedListener = InstallStateUpdatedListener { installState ->
        // Update the StateFlow value
        val newState = when (installState.installStatus()) {
            InstallStatus.DOWNLOADING -> UpdateState.DOWNLOADING
            InstallStatus.DOWNLOADED -> UpdateState.DOWNLOADED
            InstallStatus.INSTALLING -> UpdateState.INSTALLING
            InstallStatus.INSTALLED -> UpdateState.INSTALLED
            InstallStatus.CANCELED, InstallStatus.FAILED -> UpdateState.FAILED
            else -> _updateState.value
        }
        _updateState.value = newState

        if (newState == UpdateState.DOWNLOADED) {
             completeUpdate()
        }
    }

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        updateDialogLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // Handle the result of the update flow (e.g., user cancelled)
            // The listener handles success/failure during download/install
            if (result.resultCode != Activity.RESULT_OK && _updateState.value != UpdateState.INSTALLED) {
                 // Consider specific handling for cancellation if needed
                 // For now, let the listener handle FAILED state if applicable
                 // _updateState.value = UpdateState.FAILED // Or a specific CANCELED state?
            }
        }
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    // Public method to be called from Activity's onResume
    fun resumeUpdate() {
         // Check for ongoing updates or downloaded updates that need installation
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // If an immediate update is stalled, resume it.
                if (info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    startUpdate() // Try starting the flow again
                }
            } else if (info.installStatus() == InstallStatus.DOWNLOADED) {
                // If a flexible update was downloaded, prompt for install.
                 completeUpdate()
            }
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        resumeUpdate() // Call the public resume method
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        appUpdateManager.unregisterListener(installStateUpdatedListener)
        updateDialogLauncher?.unregister()
    }

    suspend fun checkForUpdate() {
        val currentVersionCode = BuildConfig.VERSION_CODE

        // Set state to CHECKING before starting
        _updateState.value = UpdateState.CHECKING

        withContext(Dispatchers.IO) {
            updateInfo = suspendCoroutine { cont ->
                appUpdateManager.appUpdateInfo.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        cont.resume(task.result)
                    } else {
                        _updateState.value = UpdateState.FAILED
                        cont.resume(null) // Resume with null on failure
                    }
                }
            }
        } // End of withContext

        // Process the result on the main thread
        updateInfo?.let { info ->
            val latestVersionCode = info.availableVersionCode()

            // Determine update type (example logic, adjust as needed)
            updateType = if (latestVersionCode - currentVersionCode > 3) {
                AppUpdateType.IMMEDIATE
            } else {
                AppUpdateType.FLEXIBLE
            }

            val availability = info.updateAvailability()
            val installStatus = info.installStatus()

            if (availability == UpdateAvailability.UPDATE_AVAILABLE && info.isUpdateTypeAllowed(updateType)) {
                _updateState.value = UpdateState.AVAILABLE
                // Don't automatically start update here, let the UI trigger it
                // startUpdate()
            } else if (availability == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                 // If already downloading/installing, reflect that state
                 if (installStatus == InstallStatus.DOWNLOADING) {
                     _updateState.value = UpdateState.DOWNLOADING
                 } else if (installStatus == InstallStatus.INSTALLING) {
                     _updateState.value = UpdateState.INSTALLING
                 } else {
                     // Default to DOWNLOADING if in progress but status unknown
                     _updateState.value = UpdateState.DOWNLOADING
                 }
            } else if (installStatus == InstallStatus.DOWNLOADED) {
                 _updateState.value = UpdateState.DOWNLOADED
                 completeUpdate() // Prompt for install if already downloaded
            } else if (availability == UpdateAvailability.UPDATE_NOT_AVAILABLE || installStatus == InstallStatus.INSTALLED) {
                _updateState.value = UpdateState.INSTALLED
            } else {
                 // Default to INSTALLED or FAILED if state is unclear
                 _updateState.value = UpdateState.INSTALLED // Or FAILED?
            }
        } ?: run {
             // If updateInfo is null after check (task failed earlier)
             if (_updateState.value == UpdateState.CHECKING) { // Avoid overwriting FAILED state set in coroutine
                 _updateState.value = UpdateState.FAILED
             }
        }
    }


     fun startUpdate() {
        updateInfo?.let { info ->
            try {
                val updateOptions = AppUpdateOptions.newBuilder(updateType).build()
                val updateStarted = appUpdateManager.startUpdateFlowForResult(
                    info,
                    activity, // Use the activity passed in constructor
                    updateOptions,
                    REQUEST_CODE
                )
                // No need to check updateStarted, listener will handle progress/failure
                // if (!updateStarted) {
                //     _updateState.value = UpdateState.FAILED
                // }
            } catch (e: Exception) {
                 // Log the exception
                _updateState.value = UpdateState.FAILED
            }
        } ?: run {
             // Cannot start update if updateInfo is null
             _updateState.value = UpdateState.FAILED
        }
    }

    // Public method to complete the update (renamed from promptToCompleteUpdate)
    fun completeUpdate() {
        // Only attempt completion if downloaded
        if (_updateState.value == UpdateState.DOWNLOADED) {
            appUpdateManager.completeUpdate().addOnFailureListener {
                // Handle failure to complete (e.g., log error)
                // State might remain DOWNLOADED or switch to FAILED based on listener
                 _updateState.value = UpdateState.FAILED // Or rely on listener?
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 100
    }
}
