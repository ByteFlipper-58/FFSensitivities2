package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * Application lifecycle observer to show app open ads when app comes to foreground
 */
class AppOpenAdLifecycleObserver(
    private val application: Application,
    private val adMobViewModel: AdMobViewModel // Inject the ViewModel
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private var currentActivity: Activity? = null
    // Removed isAppInForeground flag as ProcessLifecycleOwner handles this implicitly

    init {
        // Register activity lifecycle callbacks
        application.registerActivityLifecycleCallbacks(this)
        // Observe process lifecycle
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    // --- DefaultLifecycleObserver Implementation ---

    override fun onStart(owner: LifecycleOwner) {
        // App came to foreground
        // Use lifecycleScope tied to ProcessLifecycleOwner for safety
        owner.lifecycleScope.launch { // Use owner's scope
            currentActivity?.let { activity ->
                adMobViewModel.showAppOpenAd(activity)
            }
        }
    }

    // --- Application.ActivityLifecycleCallbacks Implementation ---

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        // Always update the currentActivity when an activity starts
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        // Update currentActivity on resume as well, in case started wasn't called first (less likely but safe)
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        // Clear currentActivity if the destroyed activity was the current one
        if (currentActivity == activity) {
            currentActivity = null
        }
    }

    // Optional: Method to unregister listeners if needed, e.g., for testing or specific scenarios
    fun unregister() {
        application.unregisterActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        currentActivity = null // Clear reference
    }
}
