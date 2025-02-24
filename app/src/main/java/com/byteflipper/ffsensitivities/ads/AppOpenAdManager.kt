package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.yandex.mobile.ads.appopenad.AppOpenAd
import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader
import com.yandex.mobile.ads.common.*

class AppOpenAdManager(private val application: Application) {
    private var appOpenAdLoader: AppOpenAdLoader? = null
    private var appOpenAd: AppOpenAd? = null
    private var isShowingAd = false
    private var currentActivity: Activity? = null
    private var loadAttempts = 0
    private val MAX_LOAD_ATTEMPTS = 3

    private val MIN_BACKGROUND_TIME = 30_000L
    private var lastBackgroundTime: Long = 0

    // Для отслеживания первого запуска
    private var isFirstLaunch = true

    private val adUnitId = "R-M-13549181-1"

    init {
        setupLifecycleObserver()
        // Сразу начинаем загружать рекламу при инициализации
        loadAd()
    }

    private fun setupLifecycleObserver() {
        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onStart(owner: androidx.lifecycle.LifecycleOwner) {
                Log.d("AppOpenAd", "App started, isFirstLaunch: $isFirstLaunch")

                if (isFirstLaunch) {
                    // При первом запуске показываем рекламу после небольшой задержки
                    currentActivity?.let { activity ->
                        Log.d("AppOpenAd", "First launch, showing ad with delay.")
                        activity.window.decorView.postDelayed({
                            showAdIfAvailable(activity)
                        }, 5000) // Задержка 1 секунда для загрузки сплэш-скрина
                    }
                    isFirstLaunch = false
                } else if (System.currentTimeMillis() - lastBackgroundTime >= MIN_BACKGROUND_TIME) {
                    // Для последующих запусков проверяем время в фоне
                    Log.d("AppOpenAd", "Not first launch, showing ad if available.")
                    currentActivity?.let { showAdIfAvailable(it) }
                }
            }

            override fun onStop(owner: androidx.lifecycle.LifecycleOwner) {
                lastBackgroundTime = System.currentTimeMillis()
                Log.d("AppOpenAd", "App stopped, saving background time: $lastBackgroundTime")
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }

    private fun loadAd() {
        if (isShowingAd || loadAttempts >= MAX_LOAD_ATTEMPTS) {
            Log.d("AppOpenAd", "Skipping ad load. isShowingAd: $isShowingAd, loadAttempts: $loadAttempts")
            return
        }

        appOpenAdLoader = appOpenAdLoader ?: AppOpenAdLoader(application)
        val adRequestConfiguration = AdRequestConfiguration.Builder(adUnitId).build()

        Log.d("AppOpenAd", "Loading ad...")
        appOpenAdLoader?.setAdLoadListener(object : AppOpenAdLoadListener {
            override fun onAdLoaded(ad: AppOpenAd) {
                loadAttempts = 0
                appOpenAd = ad
                appOpenAd?.setAdEventListener(adEventListener)
                Log.d("AppOpenAd", "Ad loaded successfully.")
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                loadAttempts++
                Log.e("AppOpenAd", "Ad failed to load: ${error.description}")
            }
        })

        appOpenAdLoader?.loadAd(adRequestConfiguration)
    }

    private val adEventListener = object : AppOpenAdEventListener {
        override fun onAdShown() {
            isShowingAd = true
            Log.d("AppOpenAd", "Ad shown.")
        }

        override fun onAdFailedToShow(error: AdError) {
            isShowingAd = false
            Log.e("AppOpenAd", "Ad failed to show: ${error.description}")
            clearAppOpenAd()
            loadAd()
        }

        override fun onAdDismissed() {
            isShowingAd = false
            Log.d("AppOpenAd", "Ad dismissed.")
            clearAppOpenAd()
            loadAd()
        }

        override fun onAdClicked() {
            // Опционально: отслеживание кликов
            Log.d("AppOpenAd", "Ad clicked.")
        }

        override fun onAdImpression(data: ImpressionData?) {
            // Опционально: отслеживание показов
            Log.d("AppOpenAd", "Ad impression.")
        }
    }

    private fun showAdIfAvailable(activity: Activity) {
        if (!isShowingAd && appOpenAd != null) {
            Log.d("AppOpenAd", "Showing ad.")
            appOpenAd?.show(activity)
        } else {
            Log.d("AppOpenAd", "Ad not available, reloading.")
            loadAd()
        }
    }

    private fun clearAppOpenAd() {
        appOpenAd?.setAdEventListener(null)
        appOpenAd = null
        Log.d("AppOpenAd", "Cleared app open ad.")
    }

    fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        Log.d("AppOpenAd", "Activity resumed: ${activity.localClassName}")
    }

    fun onActivityPaused(activity: Activity) {
        currentActivity = null
        Log.d("AppOpenAd", "Activity paused: ${activity.localClassName}")
    }
}
