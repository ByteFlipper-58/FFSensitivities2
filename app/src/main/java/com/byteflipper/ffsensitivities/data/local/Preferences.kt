package com.byteflipper.ffsensitivities.data.local

import android.content.Context
import android.content.SharedPreferences
import com.byteflipper.ffsensitivities.presentation.ui.theme.ContrastLevel

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun readString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    fun putInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    fun readInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun readBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun putFloat(key: String, value: Float) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }

    fun readFloat(key: String, defaultValue: Float = 0f): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    fun putLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    fun readLong(key: String, defaultValue: Long = 0L): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}

fun saveRequestSentStatus(context: Context, status: Boolean) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putBoolean("isRequestSent", status)
    editor.apply()
}

fun getRequestSentStatus(context: Context): Boolean {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean("isRequestSent", false)
}

fun isFirstLaunch(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean("is_first_launch", true)
}

fun setFirstLaunchCompleted(context: Context) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit()
        .putBoolean("is_first_launch", false)
        .apply()
}

class ThemePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun saveDarkMode(isDarkMode: Boolean) {
        prefs.edit().putBoolean("isDarkMode", isDarkMode).apply()
    }

    fun saveDynamicColor(isDynamicColor: Boolean) {
        prefs.edit().putBoolean("isDynamicColor", isDynamicColor).apply()
    }

    fun saveContrastLevel(level: ContrastLevel) {
        prefs.edit().putString("contrastLevel", level.name).apply()
    }

    fun getDarkMode(): Boolean =
        prefs.getBoolean("isDarkMode", false)

    fun getDynamicColor(): Boolean =
        prefs.getBoolean("isDynamicColor", false)

    fun getContrastLevel(): ContrastLevel =
        ContrastLevel.valueOf(prefs.getString("contrastLevel", ContrastLevel.None.name) ?: ContrastLevel.None.name)
}