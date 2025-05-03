package com.byteflipper.ffsensitivities

import android.app.Application
// import androidx.lifecycle.ProcessLifecycleOwner // No longer needed here
import com.byteflipper.crashhandler.CrashHandler
// import com.byteflipper.ffsensitivities.ads.AppOpenAdManager // Old manager removed
import com.byteflipper.ffsensitivities.ads.AdMobViewModel // Import new ViewModel
import com.byteflipper.ffsensitivities.ads.AppOpenAdLifecycleObserver // Import new Observer
import com.byteflipper.ffsensitivities.ads.ConsentManager
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import com.byteflipper.ffsensitivities.di.ApplicationScope
// Import MobileAds if not already present, though ConsentManager handles init
// import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {

    @Inject lateinit var dataStoreManager: DataStoreManager
    // @Inject lateinit var appOpenAdManager: AppOpenAdManager // Old manager removed
    @Inject lateinit var consentManager: ConsentManager
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    // Add fields for the new components
    private lateinit var adMobViewModel: AdMobViewModel
    private lateinit var appOpenAdLifecycleObserver: AppOpenAdLifecycleObserver

    override fun onCreate() {
        super.onCreate()

        // Initialize Mobile Ads SDK via ConsentManager and set up App Open Ad components in the callback
        consentManager.initializeMobileAdsSdk {
            // This block executes after MobileAds.initialize() completes successfully
            adMobViewModel = AdMobViewModel(this)
            appOpenAdLifecycleObserver = AppOpenAdLifecycleObserver(this, adMobViewModel)
            // The AppOpenAdLifecycleObserver constructor now handles registering itself
            // for lifecycle and activity callbacks.
        }

        // ProcessLifecycleOwner.get().lifecycle.addObserver(appOpenAdManager) // Old registration removed
        // registerActivityLifecycleCallbacks(appOpenAdManager) // Old registration removed

        val logsDir = File(getExternalFilesDir(null), "my_crash_logs")

        CrashHandler.init(this)
            .configureCrashLogsDir(logsDir)
            .enableCrashActivity(true)
            .enableLogSaving(true)
            .enableFirebaseCrashlytics(true)
    }
}
