package com.byteflipper.ffsensitivities.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Checks if the app has permission to post notifications.
 * Required for Android 13 (Tiramisu, API 33) and above.
 * Always returns true for lower API levels as the permission is granted by default.
 */
fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        // Notification permission is implicitly granted on older versions
        true
    }
}
