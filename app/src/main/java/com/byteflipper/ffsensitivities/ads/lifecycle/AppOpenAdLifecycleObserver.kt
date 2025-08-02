package com.byteflipper.ffsensitivities.ads.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.byteflipper.ffsensitivities.ads.core.AdLocation
import com.byteflipper.ffsensitivities.ads.core.AdType
import com.byteflipper.ffsensitivities.ads.AdManager
import com.byteflipper.ffsensitivities.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Отслеживает жизненный цикл приложения и показывает App Open рекламу
 * при возврате из фона или запуске приложения
 */
@Singleton
class AppOpenAdLifecycleObserver @Inject constructor(
    private val adManager: AdManager,
    @ApplicationScope private val coroutineScope: CoroutineScope
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private companion object {
        private const val TAG = "AppOpenAdObserver"
    }

    private var currentActivity: Activity? = null
    private var isShowingAd = false
    private var isAppJustStarted = true

    /**
     * Инициализирует observer и регистрирует его в ProcessLifecycle
     */
    fun initialize(application: Application) {
        Log.d(TAG, "Initializing App Open Ad Lifecycle Observer")
        
        // Регистрируем observer для отслеживания жизненного цикла процесса
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        
        // Регистрируем callbacks для отслеживания активности
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(TAG, "App moved to foreground")
        showAppOpenAdIfAvailable()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "App moved to background")
    }

    private fun showAppOpenAdIfAvailable() {
        if (isShowingAd) {
            Log.d(TAG, "App Open ad is already showing")
            return
        }

        val activity = currentActivity
        if (activity == null) {
            Log.w(TAG, "No current activity available for showing App Open ad")
            return
        }

        // Проверяем, готова ли реклама
        if (!adManager.isAdReady(AdType.APP_OPEN, AdLocation.APP_STARTUP)) {
            Log.d(TAG, "App Open ad not ready")
            // Пытаемся предзагрузить в фоновом потоке
            coroutineScope.launch {
                try {
                    adManager.loadAd(AdType.APP_OPEN, AdLocation.APP_STARTUP)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load App Open ad", e)
                }
            }
            return
        }

        Log.d(TAG, "Showing App Open ad")
        isShowingAd = true
        
        // ИСПРАВЛЕНИЕ: Показываем рекламу обязательно в главном потоке
        coroutineScope.launch {
            try {
                withContext(Dispatchers.Main) {
                    adManager.showAd(
                        adType = AdType.APP_OPEN,
                        location = AdLocation.APP_STARTUP,
                        activity = activity
                    ) { result ->
                        Log.d(TAG, "App Open ad result: success=${result.success}")
                        isShowingAd = false
                        isAppJustStarted = false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to show App Open ad", e)
                isShowingAd = false
            }
        }
    }

    // Activity Lifecycle Callbacks
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.d(TAG, "Activity created: ${activity.javaClass.simpleName}")
    }

    override fun onActivityStarted(activity: Activity) {
        Log.d(TAG, "Activity started: ${activity.javaClass.simpleName}")
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        Log.d(TAG, "Activity resumed: ${activity.javaClass.simpleName}")
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        Log.d(TAG, "Activity paused: ${activity.javaClass.simpleName}")
    }

    override fun onActivityStopped(activity: Activity) {
        Log.d(TAG, "Activity stopped: ${activity.javaClass.simpleName}")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // No-op
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.d(TAG, "Activity destroyed: ${activity.javaClass.simpleName}")
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
} 