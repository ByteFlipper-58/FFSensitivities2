package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.utils.AdConstants
import com.google.android.gms.ads.AdError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
/**
 * ViewModel for AdMob App Open Ads
 */
class AdMobViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val appOpenAdManager = AppOpenAdManager(application, AdConstants.APP_OPEN_AD_UNIT_ID)

    private val _isAdDismissed = MutableStateFlow(false)
    val isAdDismissed: StateFlow<Boolean> = _isAdDismissed.asStateFlow()

    init {
        loadAppOpenAd()

        appOpenAdManager.appOpenAdListener = object : AppOpenAdManager.AppOpenAdListener {
            override fun onAdDismissed() {
                _isAdDismissed.value = true
                viewModelScope.launch {
                    kotlinx.coroutines.delay(500)
                    _isAdDismissed.value = false
                }
            }

            override fun onAdShowedFullScreenContent() {
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            }
        }
    }

    fun loadAppOpenAd() {
        appOpenAdManager.loadAd()
    }

    fun showAppOpenAd(activity: Activity) {
        appOpenAdManager.showAdIfAvailable(activity)
    }
}
