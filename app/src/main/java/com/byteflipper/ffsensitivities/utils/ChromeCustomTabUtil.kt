package com.byteflipper.ffsensitivities.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent

object ChromeCustomTabUtil {

    private const val CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome"

    fun openUrl(context: Context, url: String) {
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setUrlBarHidingEnabled(true)
                .setShowTitle(true)
                .build()

            customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + context.packageName))

            customTabsIntent.launchUrl(context, Uri.parse(url))
        } catch (e: Exception) {
            openUrlInDefaultBrowser(context, url)
            e.printStackTrace()
        }
    }

    fun isChromeInstalled(context: Context): Boolean {
        val pm: PackageManager = context.packageManager
        return try {
            pm.getPackageInfo(CUSTOM_TAB_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun openUrlInDefaultBrowser(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "No browser found to open this URL", Toast.LENGTH_SHORT).show()
        }
    }
}