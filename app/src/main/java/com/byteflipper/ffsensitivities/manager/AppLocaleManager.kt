package com.byteflipper.ffsensitivities.manager

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

data class Language(
    val code: String, // e.g., "en", "ru"
    val displayLanguage: String // e.g., "English", "Русский"
)

// List of supported languages in the app
// Note: "system" is handled separately, not included here.
val appLanguages = listOf(
    Language("en", getDisplayName("en")),
    Language("be", getDisplayName("be")),
    Language("de", getDisplayName("de")),
    Language("fr", getDisplayName("fr")),
    Language("pl", getDisplayName("pl")),
    Language("ru", getDisplayName("ru")),
    Language("tr", getDisplayName("tr")),
    Language("uk", getDisplayName("uk"))
)

// Helper to get the display name in the language's own script
private fun getDisplayName(languageCode: String): String {
    val locale = Locale(languageCode)
    return locale.getDisplayLanguage(locale).replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(locale) else it.toString()
    }
}

class AppLocaleManager {

    /**
     * Changes the application's locale.
     * Uses LocaleManager for API 33+ and AppCompatDelegate for older versions.
     * Pass null or "system" to reset to system default.
     */
    fun changeLanguage(context: Context, languageCode: String?) {
        val localeTag = if (languageCode == "system") null else languageCode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            if (localeTag != null) {
                localeManager?.applicationLocales = LocaleList.forLanguageTags(localeTag)
            } else {
                localeManager?.applicationLocales = LocaleList.getEmptyLocaleList()
            }
        } else {
            val locales = if (localeTag != null) {
                LocaleListCompat.forLanguageTags(localeTag)
            } else {
                LocaleListCompat.getEmptyLocaleList()
            }
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }

    /**
     * Gets the current application locale code.
     * Returns "system" if the app is using the system default locale.
     */
    fun getCurrentLanguageCode(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (!locales.isEmpty) {
            locales.toLanguageTags()
        } else {
            "system"
        }
    }
}
