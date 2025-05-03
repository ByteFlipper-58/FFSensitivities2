package com.byteflipper.ffsensitivities

import android.app.Application
import com.byteflipper.crashhandler.CrashHandler
import com.byteflipper.ffsensitivities.ads.AdMobViewModel
import com.byteflipper.ffsensitivities.ads.AppOpenAdLifecycleObserver
import com.byteflipper.ffsensitivities.ads.ConsentManager
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
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    private lateinit var adMobViewModel: AdMobViewModel
    private lateinit var appOpenAdLifecycleObserver: AppOpenAdLifecycleObserver

    override fun onCreate() {
        super.onCreate()

        consentManager.initializeMobileAdsSdk {
            // This block executes after MobileAds.initialize() completes successfully
            adMobViewModel = AdMobViewModel(this)
            appOpenAdLifecycleObserver = AppOpenAdLifecycleObserver(this, adMobViewModel)
        }

        val logsDir = File(getExternalFilesDir(null), "my_crash_logs")

        CrashHandler.init(this)
            .configureCrashLogsDir(logsDir)
            .enableCrashActivity(true)
            .enableLogSaving(true)
            .enableFirebaseCrashlytics(true)
    }
}
