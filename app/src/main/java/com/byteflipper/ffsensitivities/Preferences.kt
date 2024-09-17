package com.byteflipper.ffsensitivities

import android.content.Context
import android.content.SharedPreferences

fun getPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
}

fun saveDynamicColorPreference(context: Context, enabled: Boolean) {
    getPreferences(context).edit().putBoolean("dynamic_colors", enabled).apply()
}

fun getDynamicColorPreference(context: Context): Boolean {
    return getPreferences(context).getBoolean("dynamic_colors", true)
}

fun saveThemePreference(context: Context, theme: String) {
    getPreferences(context).edit().putString("theme_preference", theme).apply()
}

fun getThemePreference(context: Context): String {
    return getPreferences(context).getString("theme_preference", "system") ?: "system"
}
