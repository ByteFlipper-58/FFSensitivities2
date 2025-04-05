package com.byteflipper.ffsensitivities

import android.app.Application
import android.content.Intent
import android.os.Process
import android.util.Log
import com.byteflipper.ffsensitivities.ads.AdManagerHolder
import com.byteflipper.ffsensitivities.presentation.ui.ErrorActivity // Will be created later
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yandex.mobile.ads.common.MobileAds
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import kotlin.system.exitProcess

@HiltAndroidApp
class MyApplication : Application() {
    //var appOpenAdManager: AppOpenAdManager? = null

    override fun onCreate() {
        super.onCreate()
        initializeLogging()
        Timber.plant(FileLoggingTree(this))
        setupCrashHandler() // Add crash handler setup

        MobileAds.initialize(this) {
           Log.d("MyApplication", "Yandex Mobile Ads initialized.")
           // Initialize AdManagerHolder after Yandex SDK is initialized
           AdManagerHolder.initialize(this)
        }
       // appOpenAdManager = AppOpenAdManager(this) // Remove or keep commented old manager
    }

    // TODO: Consider calling AdManagerHolder.destroy() in onTerminate() if needed,
    // although Application.onTerminate() is not guaranteed to be called on real devices.

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Log the crash to Crashlytics
            FirebaseCrashlytics.getInstance().recordException(throwable)
            Timber.e(throwable, "Uncaught exception") // Log locally as well

            // Start ErrorActivity
            val intent = Intent(this, ErrorActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(ErrorActivity.EXTRA_ERROR_MESSAGE, throwable.message)
                putExtra(ErrorActivity.EXTRA_ERROR_STACKTRACE, Log.getStackTraceString(throwable))
            }
            startActivity(intent)

            // Terminate the process
            Process.killProcess(Process.myPid())
            exitProcess(10) // Exit with a non-zero code

            // Optionally, call the default handler if needed (e.g., for system reporting)
            // defaultHandler?.uncaughtException(thread, throwable)
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

object Logger {
    fun d(message: String, vararg args: Any?) {
        Timber.d(message, *args)
    }

    fun i(message: String, vararg args: Any?) {
        Timber.i(message, *args)
    }

    fun w(message: String, vararg args: Any?) {
        Timber.w(message, *args)
    }

    fun e(message: String, vararg args: Any?) {
        Timber.e(message, *args)
    }

    fun e(t: Throwable, message: String? = null) {
        // Log non-fatal exceptions to Crashlytics via Timber
        Timber.e(t, message ?: "An error occurred")
    }

    fun wtf(message: String, vararg args: Any?) {
        // What a Terrible Failure - log as error
        Timber.e("WTF: $message", *args)
        // Consider logging WTF events to Crashlytics as non-fatal errors
        FirebaseCrashlytics.getInstance().recordException(Exception("WTF: $message"))
    }
}
