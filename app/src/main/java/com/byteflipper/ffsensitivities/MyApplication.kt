package com.byteflipper.ffsensitivities

import android.app.Application
import android.util.Log
import com.byteflipper.crashhandler.CrashHandler
import com.byteflipper.ffsensitivities.ads.ConsentManager
import com.byteflipper.ffsensitivities.ads.lifecycle.AppOpenAdLifecycleObserver
import com.byteflipper.ffsensitivities.ads.repository.AdRepository
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import com.byteflipper.ffsensitivities.di.ApplicationScope
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {

    @Inject lateinit var dataStoreManager: DataStoreManager
    @Inject lateinit var consentManager: ConsentManager
    @Inject lateinit var adRepository: AdRepository
    @Inject lateinit var appOpenAdObserver: AppOpenAdLifecycleObserver
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        
        initializeAdsSDK()
        initializeAppOpenAds()
        initializeCrashHandler()
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d("MyApplication", "App terminated - AdRepository state preserved")
    }

    /**
     * Инициализирует рекламный SDK и связанные компоненты
     */
    private fun initializeAdsSDK() {
        consentManager.initializeMobileAdsSdk {
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
}
