package com.byteflipper.ffsensitivities

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.byteflipper.crashhandler.CrashHandler
import com.byteflipper.ffsensitivities.ads.AppOpenAdManager
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
    @Inject lateinit var appOpenAdManager: AppOpenAdManager
    @Inject lateinit var consentManager: ConsentManager
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        consentManager.initializeMobileAdsSdk()

        ProcessLifecycleOwner.get().lifecycle.addObserver(appOpenAdManager)
        registerActivityLifecycleCallbacks(appOpenAdManager)

        val logsDir = File(getExternalFilesDir(null), "my_crash_logs")

        CrashHandler.init(this)
            .configureCrashLogsDir(logsDir)
            .enableCrashActivity(true)
            .enableLogSaving(true)
            .enableFirebaseCrashlytics(true)
    }
}
