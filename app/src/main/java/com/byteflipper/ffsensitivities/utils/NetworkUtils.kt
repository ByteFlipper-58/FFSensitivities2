package com.byteflipper.ffsensitivities.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService

/**
 * Utility object for network-related checks.
 */
object NetworkUtils {

    /**
     * Checks if the device has an active and validated internet connection.
     *
     * This method uses the modern NetworkCapabilities API available since Android M (API 23).
     * Since the project's minSdk is 27, the deprecated path is not needed.
     *
     * @param context The application context.
     * @return True if connected and validated, false otherwise.
     */
    fun isInternetConnected(context: Context): Boolean {
        // Get the ConnectivityManager system service
        val connectivityManager = context.getSystemService<ConnectivityManager>() ?: return false

        // Get the currently active network
        val network = connectivityManager.activeNetwork ?: return false

        // Get the capabilities of the active network
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        // Check if the network has internet capability and is validated (actually connected to the internet)
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
