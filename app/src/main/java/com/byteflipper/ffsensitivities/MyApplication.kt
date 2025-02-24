package com.byteflipper.ffsensitivities

import android.app.Application
import android.util.Log
import com.yandex.mobile.ads.common.MobileAds
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MyApplication : Application() {
    //var appOpenAdManager: AppOpenAdManager? = null

    override fun onCreate() {
        super.onCreate()
        initializeLogging()
        Timber.plant(FileLoggingTree(this))

        MobileAds.initialize(this) {
           Log.d("AppOpenAd", "Mobile Ads initialized")
        }
       // appOpenAdManager = AppOpenAdManager(this)
    }

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTimberTree())
        } else {
            Timber.plant(ReleaseTimberTree())
        }
    }

    private class DebugTimberTree : Timber.DebugTree() {
        override fun createStackElementTag(element: StackTraceElement): String? {
            return "(${element.fileName}:${element.lineNumber}) ${element.methodName}()"
        }

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            val modifiedTag = "YourAppDebug/$tag"
            super.log(priority, modifiedTag, message, t)
        }
    }

    private class ReleaseTimberTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.ERROR || priority == Log.ASSERT) {
                Log.println(priority, "YourAppRelease", message)
                t?.printStackTrace()
            }
        }

        override fun isLoggable(tag: String?, priority: Int): Boolean {
            return priority == Log.ERROR || priority == Log.ASSERT
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
        Timber.e(t, message ?: "An error occurred")
    }

    fun wtf(message: String, vararg args: Any?) {
        Timber.wtf(message, *args)
    }
}