package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for AdMob Interstitial Ads
 */
class InterstitialAdViewModel(
    application: Application,
    adUnitId: String,
    private val adFrequency: Int
) : AndroidViewModel(application) {

    private val interstitialAdManager = InterstitialAdManager(application.applicationContext, adUnitId)

    private val _adState = MutableStateFlow<InterstitialAdState>(InterstitialAdState.Initial)
    val adState: StateFlow<InterstitialAdState> = _adState.asStateFlow()

    private var actionCounter = 0

    private val showAdFrequency = adFrequency

    init {
        interstitialAdManager.interstitialAdListener = object : InterstitialAdManager.InterstitialAdListener {
            override fun onAdDismissed() {
                _adState.value = InterstitialAdState.Dismissed
                viewModelScope.launch {
                    kotlinx.coroutines.delay(500)
                    if (_adState.value == InterstitialAdState.Dismissed) {
                         _adState.value = InterstitialAdState.Initial
                    }
                }
            }

            override fun onAdShowedFullScreenContent() {
                _adState.value = InterstitialAdState.Shown
                actionCounter = 0
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                _adState.value = InterstitialAdState.ShowFailed(adError)
                // Attempt to load a new ad might be redundant if manager already does on failure
                // loadInterstitialAd() // Consider if needed based on manager behavior
            }

            override fun onAdLoaded() {
                _adState.value = InterstitialAdState.Loaded
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                _adState.value = InterstitialAdState.LoadFailed(error)
                // Retry loading after a delay
                viewModelScope.launch {
                    kotlinx.coroutines.delay(5000) // Wait 5 seconds before retry
                    if (_adState.value is InterstitialAdState.LoadFailed) {
                        _adState.value = InterstitialAdState.Initial // Reset state before loading
                        loadInterstitialAd()
                    }
                }
            }
        }

        loadInterstitialAd()
    }

    /**
     * Load an interstitial ad
     */
    fun loadInterstitialAd() {
        // Only transition to Loading if not already Loaded or Loading
        if (_adState.value != InterstitialAdState.Loaded && _adState.value != InterstitialAdState.Loading) {
            _adState.value = InterstitialAdState.Loading
            interstitialAdManager.loadAd()
        }
    }

    /**
     * Show an interstitial ad if available
     * @return true if ad was shown, false otherwise
     */
    fun showInterstitialAd(activity: Activity): Boolean {
        val wasShown = interstitialAdManager.showAdIfAvailable(activity)
        if (!wasShown && _adState.value == InterstitialAdState.Loaded) {
             // If manager failed to show (e.g., isShowingAd flag was true), reflect failure?
             // The example doesn't explicitly handle this transition here.
             // Let's assume the manager's callbacks handle state changes.
        } else if (!wasShown && _adState.value != InterstitialAdState.Loading) {
            // If ad wasn't ready, ensure a load is triggered
            loadInterstitialAd()
        }
        return wasShown
    }

    /**
     * Track user actions and show ad based on frequency
     * Call this method when user completes an action where showing an ad would be appropriate
     * @return true if ad was shown, false otherwise
     */
    fun trackActionAndShowAdIfNeeded(activity: Activity): Boolean {
        actionCounter++
        var adShown = false

        // Check if it's time to show an ad
        if (actionCounter >= showAdFrequency) {
            adShown = showInterstitialAd(activity)
            if (adShown) {
                // Counter is reset in onAdShowedFullScreenContent callback
            } else {
                // If ad wasn't shown (e.g., not loaded), reset counter slightly
                // so it tries again relatively soon.
                actionCounter = showAdFrequency - 1
            }
        } else if (actionCounter == showAdFrequency - 1) {
            // If we're approaching the threshold, preload the ad if it's not already loaded/loading
            if (!interstitialAdManager.isAdAvailable() && _adState.value != InterstitialAdState.Loading) {
                 loadInterstitialAd()
            }
        }

        return adShown
    }

    class Factory(
        private val application: Application,
        private val adUnitId: String,
        private val adFrequency: Int // Add adFrequency to Factory
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InterstitialAdViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return InterstitialAdViewModel(application, adUnitId, adFrequency) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
