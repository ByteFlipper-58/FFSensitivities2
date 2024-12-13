package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.getSystemService
import com.byteflipper.ffsensitivities.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@ActivityScoped
class AdsHelper @Inject constructor(
    private val activity: Activity,
    @ApplicationContext private val context: Context,
    // private val preferences: Preferences
) {
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)
    private var consentInformation: ConsentInformation? = null

    private val canRequestAd get() = consentInformation?.canRequestAds() == true
    val isMobileAdsSdkInitialized = MutableStateFlow(false)
    val canShowAd get() = isMobileAdsSdkInitialized.value && canRequestAd

    private val _interstitialAd = mutableStateOf<InterstitialAd?>(null)
    val interstitialAd: State<InterstitialAd?> = _interstitialAd

    private val _launchAd = mutableStateOf<RewardedInterstitialAd?>(null)
    val launchAd: State<RewardedInterstitialAd?> = _launchAd

    init {
        // if (preferences.isAdsEnabled) {
        //     initialize()
        // }
        initialize()
    }

    fun initialize() {
        val consentRequestParameters = ConsentRequestParameters.Builder()
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(context)
        consentInformation?.requestConsentInfoUpdate(
            activity,
            consentRequestParameters,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    activity
                ) { loadAndShowError ->
                    if (loadAndShowError != null) {
                        Timber.e(IllegalStateException("${loadAndShowError.errorCode}: ${loadAndShowError.message}"))
                    }

                    if (canRequestAd) {
                        initializeMobileAds()
                        preloadInterstitialAd()
                        preloadLaunchAd()
                    }
                }
            },
            { requestConsentError ->
                Timber.e(IllegalStateException("${requestConsentError.errorCode}: ${requestConsentError.message}"))
            })

        if (canRequestAd) {
            initializeMobileAds()
            preloadInterstitialAd()
            preloadLaunchAd()
        }
    }

    fun openAdsUmpAgreements() {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) {
            if (it != null) {
                Timber.e(IllegalStateException("${it.errorCode}: ${it.message}"))
            }
        }
    }

    private fun initializeMobileAds() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) return

        try {
            MobileAds.initialize(context) {
                isMobileAdsSdkInitialized.value = true
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun preloadInterstitialAd() {
        if (!canRequestAd || !context.isInternetConnected()) return

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            BuildConfig.INTERSTITIAL_AD_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    _interstitialAd.value = ad
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Timber.e(IllegalArgumentException(loadAdError.message))
                }
            }
        )
    }

    fun showInterstitialAd(onDismissed: () -> Unit = {}) {
        val defaultRewardListener = OnUserEarnedRewardListener {
            //
        }

        _interstitialAd.value?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onDismissed()
                preloadInterstitialAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Timber.e(IllegalStateException(adError.message))
                onDismissed()
            }
        }

        _interstitialAd.value?.show(activity) ?: onDismissed()
    }

    private fun preloadLaunchAd() {
        Timber.d("Preloading Launch Ad - canRequestAd: $canRequestAd, Internet Connected: ${context.isInternetConnected()}")

        if (!canRequestAd || !context.isInternetConnected()) {
            Timber.w("Cannot preload launch ad due to consent or connectivity restrictions")
            return
        }

        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(
            context,
            BuildConfig.APP_OPEN_AD_ID,
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    Timber.d("Launch Ad loaded successfully")
                    _launchAd.value = ad
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Timber.e("Launch Ad Failed to Load - Error Code: ${loadAdError.code}, Message: ${loadAdError.message}")
                }
            }
        )
    }

    fun showLaunchAd(onDismissed: () -> Unit = {}) {
        Timber.d("Attempting to show Launch Ad - Ad Exists: ${_launchAd.value != null}")

        val defaultRewardListener = OnUserEarnedRewardListener {
            Timber.d("Reward earned from launch ad")
            // TODO: Add reward handling
        }

        _launchAd.value?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Timber.d("Launch Ad dismissed")
                onDismissed()
                preloadLaunchAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Timber.e("Launch Ad Failed to Show - Error: ${adError.message}")
                onDismissed()
            }
        }

        _launchAd.value?.show(activity, defaultRewardListener) ?: run {
            Timber.w("No launch ad available to show")
            onDismissed()
        }
    }

    suspend fun getSupportAd(): RewardedInterstitialAd? {
        if (!canRequestAd) return null
        if (!context.isInternetConnected()) {
            throw UnknownHostException()
        }

        val adRequest = AdRequest.Builder().build()

        return suspendCoroutine {
            RewardedInterstitialAd.load(
                context,
                BuildConfig.INTERSTITIAL_AD_ID,
                adRequest,
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(rewardedInterstitialAd: RewardedInterstitialAd) {
                        it.resume(rewardedInterstitialAd)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        it.resumeWithException(IllegalArgumentException(loadAdError.message))
                    }
                })
        }
    }

    @Composable
    fun AdBanner(width: Int) {
        val context = LocalContext.current
        val isInspectionMode = LocalInspectionMode.current

        if (isInspectionMode) return

        // Проверяем, можно ли показывать рекламу
        if (!canShowAd) return

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
}

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