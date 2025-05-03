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

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        owner.lifecycleScope.launch {
            currentActivity?.let { activity ->
                adMobViewModel.showAppOpenAd(activity)
            }
        }
    }

    // --- Application.ActivityLifecycleCallbacks Implementation ---

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }

    fun unregister() {
        application.unregisterActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        currentActivity = null
    }
}
