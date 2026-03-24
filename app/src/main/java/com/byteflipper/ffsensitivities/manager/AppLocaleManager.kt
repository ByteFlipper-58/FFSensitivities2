package com.byteflipper.ffsensitivities.manager

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
    val locale = Locale.forLanguageTag(languageCode)
    return locale.getDisplayLanguage(locale).replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(locale) else it.toString()
    }
}

class AppLocaleManager {

    /**
     * Changes the application's locale using the AndroidX per-app language API.
     * Pass null or "system" to reset to system default.
     */
    fun changeLanguage(languageCode: String?) {
        val localeTag = if (languageCode == "system") null else languageCode
        val locales = if (localeTag != null) {
            LocaleListCompat.forLanguageTags(localeTag)
        } else {
            LocaleListCompat.getEmptyLocaleList()
        }
        AppCompatDelegate.setApplicationLocales(locales)
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
