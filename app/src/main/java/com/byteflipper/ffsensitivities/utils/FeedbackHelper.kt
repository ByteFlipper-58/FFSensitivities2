package com.byteflipper.ffsensitivities.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byteflipper.ffsensitivities.BuildConfig
import com.byteflipper.ffsensitivities.R

object FeedbackHelper {

    fun SendEmail(context: Context) {
        val deviceInfo = getDeviceInfo(context)
        val appInfo = getAppInfo(context)
        val body = "$appInfo\n\n$deviceInfo"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Mailto scheme
            putExtra(Intent.EXTRA_EMAIL, arrayOf("byteflipper.business@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.bug_report_title))
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            context.startActivity(Intent.createChooser(intent, ""))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "No email clients found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDeviceInfo(context: Context): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val androidVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Build.VERSION.RELEASE_OR_CODENAME
        } else {
            Build.VERSION.RELEASE
        }
        val sdkInt = Build.VERSION.SDK_INT

        return "Device Info:\n" +
                "Manufacturer: $manufacturer\n" +
                "Model: $model\n" +
                "Android Version: $androidVersion (API $sdkInt)\n" +
                "Connection Type: ${getNetworkClass(context)}"
    }

    private fun getAppInfo(context: Context): String {
        return "\n\n\n" +
                "App Info:\n" +
                context.getString(R.string.app_name) + "\n" +
                BuildConfig.APPLICATION_ID + "\n" +
                "App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n" +
                "Build Type: ${BuildConfig.BUILD_TYPE}"
    }

    private fun getNetworkClass(context: Context): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = cm.activeNetworkInfo

        if (info == null || !info.isConnected)
            return "Not Connected" // not connected

        return when (info.type) {
            ConnectivityManager.TYPE_WIFI -> "WIFI"
            ConnectivityManager.TYPE_MOBILE -> {
                when (info.subtype) {
                    TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE,
                    TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT,
                    TelephonyManager.NETWORK_TYPE_IDEN, TelephonyManager.NETWORK_TYPE_GSM -> "2G"
                    TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0,
                    TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA,
                    TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA,
                    TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD,
                    TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G"
                    TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> "4G"
                    TelephonyManager.NETWORK_TYPE_NR -> "5G"
                    else -> "Unknown"
                }
            }
            else -> "?"
        }
    }
}