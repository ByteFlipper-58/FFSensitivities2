package com.byteflipper.ffsensitivities

import android.app.Application
import android.content.Intent
import android.os.Process
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.byteflipper.crashhandler.CrashHandler
import com.byteflipper.ffsensitivities.ads.AppOpenAdManager
import com.byteflipper.ffsensitivities.ads.ConsentManager // Import ConsentManager
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import com.byteflipper.ffsensitivities.di.ApplicationScope
import com.byteflipper.ffsensitivities.presentation.ui.ErrorActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.system.exitProcess

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

        initializeLogging()
        Timber.plant(FileLoggingTree(this))

        val logsDir = File(getExternalFilesDir(null), "my_crash_logs")

        CrashHandler.init(this)
            .configureCrashLogsDir(logsDir)
            .enableCrashActivity(true)
            .enableLogSaving(true)
            .enableFirebaseCrashlytics(true)
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            FirebaseCrashlytics.getInstance().recordException(throwable)
            Timber.e(throwable, "Uncaught exception")

            val intent = Intent(this, ErrorActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(ErrorActivity.EXTRA_ERROR_MESSAGE, throwable.message)
                putExtra(ErrorActivity.EXTRA_ERROR_STACKTRACE, Log.getStackTraceString(throwable))
            }
            startActivity(intent)

            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }


    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTimberTree())
        } else {
            // In release builds, Crashlytics will handle crash reporting.
            // We might still want a Timber tree for non-fatal errors or specific logs.
            // Let's keep ReleaseTimberTree but ensure it doesn't log FATAL crashes
            // as Crashlytics already does that.
            Timber.plant(ReleaseTimberTree())
        }
    }

    private class DebugTimberTree : Timber.DebugTree() {
        override fun createStackElementTag(element: StackTraceElement): String? {
            return "(${element.fileName}:${element.lineNumber}) ${element.methodName}()"
        }

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            val modifiedTag = "FFSensitivitiesDebug/$tag" // Changed tag prefix
            super.log(priority, modifiedTag, message, t)
        }
    }

    private class ReleaseTimberTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // Log only warnings and errors, but not ASSERT (which indicates a crash)
            if (priority == Log.WARN || priority == Log.ERROR) {
                // Log non-fatal errors to Crashlytics
                if (t != null && priority == Log.ERROR) {
                    FirebaseCrashlytics.getInstance().recordException(t)
                } else if (priority == Log.ERROR) {
                    // Log error messages without exceptions if needed
                     FirebaseCrashlytics.getInstance().log("E/$tag: $message")
                }
                // Optionally log to Logcat as well for release builds if desired
                // Log.println(priority, "FFSensitivitiesRelease/$tag", message)
                // t?.printStackTrace() // Avoid printing stack trace in release logs directly
            }
            // We don't explicitly log ASSERT here because the UncaughtExceptionHandler handles it.
        }

        override fun isLoggable(tag: String?, priority: Int): Boolean {
            // Log warnings and errors in release builds
            return priority == Log.WARN || priority == Log.ERROR
        }
    }
}