package com.byteflipper.ffsensitivities.ads

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError

/**
 * Interstitial Ad state for Compose
 */
sealed class InterstitialAdState {
    object Initial : InterstitialAdState()
    object Loading : InterstitialAdState()
    object Loaded : InterstitialAdState()
    object Shown : InterstitialAdState()
    object Dismissed : InterstitialAdState()
    data class LoadFailed(val error: LoadAdError) : InterstitialAdState()
    data class ShowFailed(val error: AdError) : InterstitialAdState()
}
