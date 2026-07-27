package com.byteflipper.ffsensitivities.ads.policy

import android.content.Context
import com.byteflipper.ffsensitivities.ads.core.AdType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps full-screen ads predictable and independent from UI click handlers.
 */
@Singleton
class AdDisplayPolicy @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences = context.getSharedPreferences("ad_display_policy", Context.MODE_PRIVATE)

    fun registerAppLaunch() {
        preferences.edit()
            .putInt(KEY_APP_LAUNCHES, preferences.getInt(KEY_APP_LAUNCHES, 0) + 1)
            .apply()
    }

    fun canShowAppOpen(backgroundDurationMs: Long): Boolean = synchronized(this) {
        val now = System.currentTimeMillis()
        preferences.getInt(KEY_APP_LAUNCHES, 0) >= MIN_LAUNCHES_BEFORE_APP_OPEN &&
            backgroundDurationMs >= MIN_BACKGROUND_DURATION_MS &&
            now - preferences.getLong(KEY_LAST_APP_OPEN_AT, 0L) >= APP_OPEN_COOLDOWN_MS &&
            canShowAppOpenForToday(now)
    }

    fun recordCompletedSensitivityView(): Boolean = synchronized(this) {
        val completed = preferences.getInt(KEY_COMPLETED_SENSITIVITY_VIEWS, 0) + 1
        preferences.edit().putInt(KEY_COMPLETED_SENSITIVITY_VIEWS, completed).apply()
        completed >= SENSITIVITY_VIEWS_PER_INTERSTITIAL && canShowInterstitial(System.currentTimeMillis())
    }

    fun recordFullScreenShown(adType: AdType) = synchronized(this) {
        val now = System.currentTimeMillis()
        preferences.edit()
            .putLong(KEY_LAST_FULLSCREEN_AT, now)
            .apply()
        when (adType) {
            AdType.INTERSTITIAL -> {
                incrementDailyCount(KEY_INTERSTITIAL_DAY, KEY_INTERSTITIAL_COUNT)
                preferences.edit().putInt(KEY_COMPLETED_SENSITIVITY_VIEWS, 0).apply()
            }
            AdType.APP_OPEN -> {
                incrementDailyCount(KEY_APP_OPEN_DAY, KEY_APP_OPEN_COUNT)
                preferences.edit().putLong(KEY_LAST_APP_OPEN_AT, now).apply()
            }
            else -> Unit
        }
    }

    private fun canShowInterstitial(now: Long): Boolean {
        return dailyCount(KEY_INTERSTITIAL_DAY, KEY_INTERSTITIAL_COUNT) < MAX_INTERSTITIAL_ADS_PER_DAY &&
            canShowAnotherFullScreen(now)
    }

    private fun canShowAppOpenForToday(now: Long): Boolean {
        return dailyCount(KEY_APP_OPEN_DAY, KEY_APP_OPEN_COUNT) < MAX_APP_OPEN_ADS_PER_DAY &&
            canShowAnotherFullScreen(now)
    }

    private fun canShowAnotherFullScreen(now: Long): Boolean {
        return now - preferences.getLong(KEY_LAST_FULLSCREEN_AT, 0L) >= FULLSCREEN_COOLDOWN_MS
    }

    private fun dailyCount(dayKey: String, countKey: String): Int {
        val today = LocalDate.now().toString()
        return if (preferences.getString(dayKey, null) == today) {
            preferences.getInt(countKey, 0)
        } else {
            0
        }
    }

    private fun incrementDailyCount(dayKey: String, countKey: String) {
        val today = LocalDate.now().toString()
        val nextCount = dailyCount(dayKey, countKey) + 1
        preferences.edit()
            .putString(dayKey, today)
            .putInt(countKey, nextCount)
            .apply()
    }

    private companion object {
        const val MIN_LAUNCHES_BEFORE_APP_OPEN = 1
        const val MIN_BACKGROUND_DURATION_MS = 30_000L
        const val APP_OPEN_COOLDOWN_MS = 4 * 60 * 60 * 1000L
        const val FULLSCREEN_COOLDOWN_MS = 2 * 60 * 1000L
        const val MAX_INTERSTITIAL_ADS_PER_DAY = 7
        const val MAX_APP_OPEN_ADS_PER_DAY = 3
        const val SENSITIVITY_VIEWS_PER_INTERSTITIAL = 3

        const val KEY_APP_LAUNCHES = "app_launches"
        const val KEY_LAST_APP_OPEN_AT = "last_app_open_at"
        const val KEY_LAST_FULLSCREEN_AT = "last_fullscreen_at"
        const val KEY_INTERSTITIAL_DAY = "interstitial_day"
        const val KEY_INTERSTITIAL_COUNT = "interstitial_count"
        const val KEY_APP_OPEN_DAY = "app_open_day"
        const val KEY_APP_OPEN_COUNT = "app_open_count"
        const val KEY_COMPLETED_SENSITIVITY_VIEWS = "completed_sensitivity_views"
    }
}
