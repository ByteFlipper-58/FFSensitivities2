package com.byteflipper.ffsensitivities.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.byteflipper.ffsensitivities.R

object ChromeCustomTabUtil {

    private const val TAG = "ChromeCustomTabUtil"

    /**
     * Opens a URL in a Chrome Custom Tab or falls back to the default browser.
     *
     * @param context The application context.
     * @param url The URL to open.
     * @param primaryColor The primary color from the current MaterialTheme (as ARGB Int).
     */
    fun openUrl(context: Context, url: String, primaryColor: Int) {
        val uri = Uri.parse(url)
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(
                    CustomTabColorSchemeParams.Builder()
                        .setToolbarColor(primaryColor)
                        .build()
                )
                .setShowTitle(true)
                // Add custom animations (optional, requires anim resources)
                // .setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
                // .setExitAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .build()

            customTabsIntent.intent.putExtra(
                Intent.EXTRA_REFERRER,
                Uri.parse("android-app://" + context.packageName)
            )

            customTabsIntent.launchUrl(context, uri)

        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "Custom Tabs not supported or no browser found: ${e.message}")
            openUrlInDefaultBrowser(context, uri)
        } catch (e: Exception) {
            // Handle other potential exceptions during Custom Tab launch
            Log.e(TAG, "Error launching Custom Tab: ${e.message}", e)
            openUrlInDefaultBrowser(context, uri)
        }
    }

    private fun openUrlInDefaultBrowser(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "No browser found to open URL: ${e.message}")
            Toast.makeText(
                context,
                context.getString(R.string.error_no_browser_found),
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error opening URL in default browser: ${e.message}", e)
            Toast.makeText(
                context,
                context.getString(R.string.error_no_browser_found),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
