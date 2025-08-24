package com.byteflipper.ffsensitivities

import android.app.Application
import android.util.Log
import com.byteflipper.crashhandler.CrashHandler
import com.byteflipper.ffsensitivities.ads.AdManager
import com.byteflipper.ffsensitivities.ads.lifecycle.AppOpenAdLifecycleObserver
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import com.byteflipper.ffsensitivities.di.ApplicationScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {

    @Inject lateinit var dataStoreManager: DataStoreManager
    @Inject lateinit var adManager: AdManager
    @Inject lateinit var appOpenAdObserver: AppOpenAdLifecycleObserver
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        
        initializeTimber()
        initializeAdsSDK()
        initializeAppOpenAds()
        initializeCrashHandler()
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d("MyApplication", "App terminated - AdManager state preserved")
    }

    /**
     * Инициализирует рекламный SDK и связанные компоненты
     */
    private fun initializeAdsSDK() {
        adManager.initializeMobileAdsSdk {
        }
    }

    /**
     * Инициализирует App Open рекламу при запуске приложения
     */
    private fun initializeAppOpenAds() {
        appOpenAdObserver.initialize(this)
    }

    /**
     * Инициализирует обработчик аварийных ситуаций
     */
    private fun initializeCrashHandler() {
        val logsDir = File(getExternalFilesDir(null), "my_crash_logs")

        CrashHandler.init(this)
            .configureCrashLogsDir(logsDir)
            .enableCrashActivity(true)
            .enableLogSaving(true)
            .enableFirebaseCrashlytics(true)
    }

    private fun initializeTimber() {
        try {
            // If you later add Timber dependency, you can plant CrashlyticsTree here
            // Timber.plant(object : Timber.Tree() {
            //     override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            //         if (priority >= Log.WARN && t != null) {
            //             FirebaseCrashlytics.getInstance().recordException(t)
            //         }
            //     }
            // })
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        } catch (_: Exception) {
        }
    }
}