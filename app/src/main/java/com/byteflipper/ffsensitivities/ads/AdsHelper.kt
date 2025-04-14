package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.getSystemService
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import android.util.Log
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@ActivityScoped
class AdsHelper @Inject constructor(
    private val activity: Activity,
    @ApplicationContext private val context: Context
) {
    private companion object {
        private const val TAG = "AdsHelper"
    }
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)
    private var consentInformation: ConsentInformation? = null

    private val canRequestAd get() = consentInformation?.canRequestAds() ?: false
    val isMobileAdsSdkInitialized = MutableStateFlow(false)
    val canShowAd get() = isMobileAdsSdkInitialized.value && canRequestAd

    init {
        initialize()
    }

    fun initialize() {
        Log.d(TAG, "Initializing AdsHelper")
        val consentRequestParameters = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(context)
        consentInformation?.requestConsentInfoUpdate(
            activity,
            consentRequestParameters,
            { handleConsentUpdateSuccess() },
            { requestConsentError ->
                Log.e(
                    TAG,
                    String.format(
                        "Consent Request Error - Code: %d, Message: %s",
                        requestConsentError.errorCode,
                        requestConsentError.message
                    )
                )
            }
        )
    }

    private fun handleConsentUpdateSuccess() {
        Log.d(TAG, "Consent information updated successfully")

        val consentStatus = consentInformation?.consentStatus ?: ConsentInformation.ConsentStatus.UNKNOWN
        when (consentStatus) {
            ConsentInformation.ConsentStatus.REQUIRED -> {
                if (consentInformation?.isConsentFormAvailable == true) {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { loadAndShowError ->
                        if (loadAndShowError != null) {
                            Log.e(
                                TAG,
                                String.format(
                                    "Consent Form Error - Code: %d, Message: %s",
                                    loadAndShowError.errorCode,
                                    loadAndShowError.message
                                )
                            )
                        } else {
                            Log.d(TAG, "Consent form displayed successfully")
                        }
                    }
                } else {
                    Log.w(TAG, "Consent form is not available")
                }
            }
            ConsentInformation.ConsentStatus.OBTAINED -> {
                Log.d(TAG, "Consent obtained - Ads can be requested")
                //initializeMobileAds()
                //preloadInterstitialAd()
                //preloadAppOpenAd()
            }
            ConsentInformation.ConsentStatus.NOT_REQUIRED -> {
                Log.d(TAG, "Consent is not required - Ads can be requested")
                //initializeMobileAds()
                //preloadInterstitialAd()
                //preloadAppOpenAd()
            }
            else -> {
                Log.w(TAG, "Consent status unknown")
            }
        }
    }

    fun isUserConsentGiven(): Boolean {
        return consentInformation?.consentStatus == ConsentInformation.ConsentStatus.OBTAINED
    }

    /*private fun initializeMobileAds() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            Log.d(TAG, "MobileAds already initialized")
            return
        }

        try {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "MobileAds initialization complete")
                isMobileAdsSdkInitialized.value = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MobileAds", e)
            isMobileAdsSdkInitialized.value = false
        }
    }

    fun retryInitialization() {
        Log.d(TAG, "Retrying AdsHelper initialization")
        isMobileAdsInitializeCalled.set(false)
        initialize()
    }

    private fun preloadInterstitialAd() {
        if (!canShowAd || !context.isInternetConnected()) {
            Log.w(TAG, "Cannot preload interstitial ad")
            return
        }

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            BuildConfig.INTERSTITIAL_AD_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    _interstitialAd.value = ad
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(
                        TAG,
                        String.format(
                            "Interstitial Ad Load Error - Code: %d, Message: %s",
                            loadAdError.code,
                            loadAdError.message
                        )
                    )
                }
            }
        )
    }

    fun showInterstitialAd(onDismissed: () -> Unit = {}) {
        if (!canShowAd) {
            Log.w(TAG, "Cannot show interstitial ad")
            onDismissed()
            return
        }

        _interstitialAd.value?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial ad dismissed")
                onDismissed()
                preloadInterstitialAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, String.format("Interstitial Ad Show Error - Message: %s", adError.message))
                onDismissed()
            }
        }

        _interstitialAd.value?.show(activity) ?: onDismissed()
    }

    private fun preloadAppOpenAd() {
        if (!canShowAd || !context.isInternetConnected()) {
            Log.w(TAG, "Cannot preload app open ad")
            return
        }

        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            BuildConfig.APP_OPEN_TEST_AD_ID,
            adRequest,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "App Open Ad loaded successfully")
                    _appOpenAd.value = ad
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(
                        TAG,
                        String.format(
                            "App Open Ad Load Error - Code: %d, Message: %s",
                            loadAdError.code,
                            loadAdError.message
                        )
                    )
                }
            }
        )
    }

    fun showAppOpenAd(onDismissed: () -> Unit = {}) {
        if (_appOpenAd.value == null || !canShowAd) {
            Log.w(TAG, "Cannot show app open ad")
            onDismissed()
            return
        }

        _appOpenAd.value?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "App Open Ad dismissed")
                onDismissed()
                preloadAppOpenAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, String.format("App Open Ad Show Error - Message: %s", adError.message))
                onDismissed()
            }
        }

        _appOpenAd.value?.show(activity)
    }

    @Composable
    fun AdBanner(width: Int) {
        val context = LocalContext.current
        val isInspectionMode = LocalInspectionMode.current

        if (isInspectionMode || !canShowAd) return

        val adRequest = AdRequest.Builder().build()

        AndroidView(
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.getPortraitAnchoredAdaptiveBannerAdSize(ctx, width))
                    adUnitId = BuildConfig.ADMOB_BANNER_AD_ID
                }
            },
            update = { adView ->
                adView.loadAd(adRequest)
            }
        )
    }
}*/
    @Suppress("DEPRECATION")
    private fun Context.isInternetConnected(): Boolean {
        val connectivityManager = getSystemService<ConnectivityManager>() ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val currentNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(currentNetwork)

            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        } else {
            connectivityManager.activeNetworkInfo?.isConnected == true
        }
    }
}
