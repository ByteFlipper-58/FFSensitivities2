package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.byteflipper.ffsensitivities.utils.AdConstants // Add import
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader

@Composable
fun YandexBannerAd(
    adUnitId: String,
    modifier: Modifier = Modifier,
    onAdLoaded: () -> Unit = {}
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var bannerAd by remember { mutableStateOf<BannerAdView?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                bannerAd?.destroy()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            bannerAd?.destroy()
        }
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally),
        factory = { context ->
            BannerAdView(context).apply {
                setAdUnitId(adUnitId)
                setAdSize(BannerAdSize.fixedSize(context, AdConstants.BANNER_WIDTH, AdConstants.BANNER_HEIGHT)) // Use constants
                setBannerAdEventListener(object : BannerAdEventListener {
                    override fun onAdLoaded() {
                        Log.d("YandexBannerAd", "Banner ad loaded successfully for $adUnitId")
                        onAdLoaded() // Call the external callback
                    }
                    override fun onAdFailedToLoad(error: AdRequestError) {
                        Log.e("YandexBannerAd", "Banner ad failed to load for $adUnitId: ${error.code} - ${error.description}")
                    }
                    override fun onAdClicked() {
                        Log.d("YandexBannerAd", "Banner ad clicked for $adUnitId")
                    }
                    override fun onLeftApplication() {}
                    override fun onReturnedToApplication() {}
                    override fun onImpression(data: ImpressionData?) {}
                })
                loadAd(AdRequest.Builder().build())
                bannerAd = this
            }
        }
    )
}

abstract class BaseAdManager<AdLoader, Ad, AdLoadListener, AdEventListener>(
    protected val context: Context, // Changed from Activity to Context
    private val adTypeTag: String
) {
    protected var ad: Ad? = null
    protected var adLoader: AdLoader? = null
    private var isLoading: Boolean = false
    private var currentAdUnitId: String? = null

    protected abstract fun createAdLoader(): AdLoader
    protected abstract fun setAdLoadListener(loader: AdLoader, listener: AdLoadListener)
    protected abstract fun clearAdLoadListener(loader: AdLoader?): Unit
    protected abstract fun loadAdInternal(loader: AdLoader, adRequest: AdRequestConfiguration)
    protected abstract fun setAdEventListener(ad: Ad, listener: AdEventListener?)
    protected abstract fun clearAdEventListener(ad: Ad?): Unit
    protected abstract fun showAdInternal(ad: Ad, activity: Activity)
    protected abstract fun createAdLoadListener(
        onLoaded: (Ad) -> Unit,
        onError: (AdRequestError) -> Unit
    ): AdLoadListener
    protected abstract fun createAdEventListener(
        onShown: () -> Unit = {},
        onDismissed: () -> Unit = {},
        onFailedToShow: (com.yandex.mobile.ads.common.AdError) -> Unit = {},
        onClicked: () -> Unit = {},
        onImpression: (ImpressionData?) -> Unit = {},
        onRewarded: (Reward) -> Unit = {}
    ): AdEventListener


    // Store default callbacks used during load for preloading
    private var defaultOnShown: () -> Unit = {}
    private var defaultOnDismissed: () -> Unit = {}
    private var defaultOnRewarded: (Reward) -> Unit = {}

    fun loadAd(
        adUnitId: String,
        onLoaded: () -> Unit = {},
        onError: (AdRequestError) -> Unit = {},
        onShown: () -> Unit = {},
        onDismissed: () -> Unit = {},
        onRewarded: (Reward) -> Unit = {}
    ) {
        // Store callbacks provided during load, primarily for preloading scenarios
        defaultOnShown = onShown
        defaultOnDismissed = onDismissed
        defaultOnRewarded = onRewarded

        // Prevent concurrent loading
        if (isLoading || ad != null) {
            Log.d(adTypeTag, "Ad is already loaded or loading.")
            // If ad is loaded and the request is for the same unit, trigger onLoaded immediately?
            if (ad != null && adUnitId == currentAdUnitId) {
                 onLoaded() // Or maybe not, depends on desired behavior
            }
            return
        }
        isLoading = true
        currentAdUnitId = adUnitId // Store for potential preload

        if (adLoader == null) {
            adLoader = createAdLoader()
        }
        val currentAdLoader = adLoader ?: run {
            isLoading = false
            return // Should not happen
        }

        val loadListener = createAdLoadListener(
            onLoaded = { loadedAd ->
                isLoading = false
                ad = loadedAd
                val eventListener = createAdEventListener(
                    onShown = onShown,
                    onDismissed = {
                        onDismissed()
                        clearAdEventListener(ad)
                        ad = null // Clear ad reference after dismissal
                        // Attempt to preload next ad
                        preloadNextAd()
                    },
                    onFailedToShow = { error ->
                        Log.e(adTypeTag, "$adTypeTag ad failed to show: ${error.description}")
                        clearAdEventListener(ad)
                        ad = null
                        // Attempt to preload next ad
                        preloadNextAd()
                    },
                    onClicked = { /* Handle click if needed */ },
                    onImpression = { /* Handle impression if needed */ },
                    onRewarded = onRewarded // Pass rewarded callback
                )
                setAdEventListener(loadedAd, eventListener)
                onLoaded() // Notify external listener
            },
            onError = { error ->
                isLoading = false
                Log.e(adTypeTag, "$adTypeTag ad failed to load: ${error.description}")
                onError(error) // Notify external listener
                // No automatic preload on load failure to avoid loops/spamming
            }
        )
        setAdLoadListener(currentAdLoader, loadListener)

        val adRequest = AdRequestConfiguration.Builder(adUnitId).build()
        loadAdInternal(currentAdLoader, adRequest)
    }

    /**
     * Shows the ad using specific callbacks for this show instance.
     * Falls back to default callbacks if none are provided.
     */
    fun show(
        activity: Activity,
        onShown: (() -> Unit)? = null,
        onDismissed: (() -> Unit)? = null,
        onRewarded: ((Reward) -> Unit)? = null // Specific to Rewarded
    ) {
        val currentAd = ad
        if (currentAd != null) {
            // Create a specific listener for this show call, using provided or default callbacks
            val eventListener = createAdEventListener(
                onShown = onShown ?: defaultOnShown,
                onDismissed = {
                    (onDismissed ?: defaultOnDismissed)() // Call provided or default dismiss
                    clearAdEventListener(ad) // Clean up listener
                    ad = null // Clear ad reference
                    preloadNextAd() // Trigger preload for next ad
                },
                onFailedToShow = { error ->
                    Log.e(adTypeTag, "$adTypeTag ad failed to show: ${error.description}")
                    clearAdEventListener(ad)
                    ad = null
                    preloadNextAd() // Trigger preload for next ad
                },
                onRewarded = onRewarded ?: defaultOnRewarded // Use provided or default reward callback
            )
            setAdEventListener(currentAd, eventListener) // Set the listener just before showing

            showAdInternal(currentAd, activity) // Show the ad

            // Clear the ad reference *after* initiating show,
            // as the listener now handles cleanup on dismissal/failure.
            // We keep 'ad' non-null until dismissal/failure ensures listener is called.
            // ad = null // Moved clearing to onDismissed/onFailedToShow in the listener
        } else {
            Log.w(adTypeTag, "$adTypeTag ad is not loaded yet, cannot show.")
            // Optionally, trigger a load here if needed
            preloadNextAd() // Try preloading if show is called when ad is not ready
        }
    } // End of show method

    // --- Preloading Logic --- Ensure this is a member function of the class, correctly placed ---
    private fun preloadNextAd() {
        if (isLoading || ad != null) {
            // Don't preload if already loading or an ad is ready
            return
        }
        val adUnitIdToLoad = currentAdUnitId
        if (adUnitIdToLoad != null) {
            Log.d(adTypeTag, "Preloading next ad for $adUnitIdToLoad")
            // Use empty callbacks for preload, errors will be logged internally
            loadAd(adUnitId = adUnitIdToLoad, onLoaded = {
                Log.d(adTypeTag, "Preloaded ad loaded successfully for $adUnitIdToLoad")
            }, onError = {
                Log.e(adTypeTag, "Preloaded ad failed to load for $adUnitIdToLoad")
                // Optionally implement retry logic here
            })
        } else {
            Log.w(adTypeTag, "Cannot preload ad, adUnitId is unknown.")
        }
    }
    // --- End Preloading Logic ---


    open fun destroy() {
        isLoading = false
        currentAdUnitId = null
        clearAdLoadListener(adLoader)
        adLoader = null
        clearAdEventListener(ad)
        ad = null
    }
}

// --- Interstitial Ad Manager ---

class InterstitialAdManager(context: Context) : BaseAdManager<InterstitialAdLoader, InterstitialAd, InterstitialAdLoadListener, InterstitialAdEventListener>(context, "InterstitialAdManager") {

    override fun createAdLoader(): InterstitialAdLoader = InterstitialAdLoader(context)
    override fun setAdLoadListener(loader: InterstitialAdLoader, listener: InterstitialAdLoadListener) { loader.setAdLoadListener(listener) }
    // Ensure Unit return type explicitly for overridden methods
    override fun clearAdLoadListener(loader: InterstitialAdLoader?): Unit { loader?.setAdLoadListener(null) }
    override fun loadAdInternal(loader: InterstitialAdLoader, adRequest: AdRequestConfiguration) { loader.loadAd(adRequest) }
    override fun setAdEventListener(ad: InterstitialAd, listener: InterstitialAdEventListener?) { ad.setAdEventListener(listener) }
    // Ensure Unit return type explicitly for overridden methods
    override fun clearAdEventListener(ad: InterstitialAd?): Unit { ad?.setAdEventListener(null) }
    override fun showAdInternal(ad: InterstitialAd, activity: Activity) { ad.show(activity) }

    override fun createAdLoadListener(
        onLoaded: (InterstitialAd) -> Unit,
        onError: (AdRequestError) -> Unit
    ): InterstitialAdLoadListener = object : InterstitialAdLoadListener {
        override fun onAdLoaded(loadedAd: InterstitialAd) = onLoaded(loadedAd)
        override fun onAdFailedToLoad(error: AdRequestError) = onError(error)
    }

    override fun createAdEventListener(
        onShown: () -> Unit,
        onDismissed: () -> Unit,
        onFailedToShow: (com.yandex.mobile.ads.common.AdError) -> Unit,
        onClicked: () -> Unit,
        onImpression: (ImpressionData?) -> Unit,
        onRewarded: (Reward) -> Unit // Not used for Interstitial
    ): InterstitialAdEventListener = object : InterstitialAdEventListener {
        override fun onAdShown() = onShown()
        override fun onAdDismissed() = onDismissed()
        override fun onAdFailedToShow(error: com.yandex.mobile.ads.common.AdError) = onFailedToShow(error)
        override fun onAdClicked() = onClicked()
        override fun onAdImpression(data: ImpressionData?) = onImpression(data)
    }
}

// --- Rewarded Ad Manager ---

class RewardedAdManager(context: Context) : BaseAdManager<RewardedAdLoader, RewardedAd, RewardedAdLoadListener, RewardedAdEventListener>(context, "RewardedAdManager") {

    override fun createAdLoader(): RewardedAdLoader = RewardedAdLoader(context)
    override fun setAdLoadListener(loader: RewardedAdLoader, listener: RewardedAdLoadListener) { loader.setAdLoadListener(listener) }
    override fun clearAdLoadListener(loader: RewardedAdLoader?): Unit { loader?.setAdLoadListener(null) }
    override fun loadAdInternal(loader: RewardedAdLoader, adRequest: AdRequestConfiguration) { loader.loadAd(adRequest) }
    override fun setAdEventListener(ad: RewardedAd, listener: RewardedAdEventListener?) { ad.setAdEventListener(listener) }
    override fun clearAdEventListener(ad: RewardedAd?): Unit { ad?.setAdEventListener(null) }
    override fun showAdInternal(ad: RewardedAd, activity: Activity) { ad.show(activity) }

    override fun createAdLoadListener(
        onLoaded: (RewardedAd) -> Unit,
        onError: (AdRequestError) -> Unit
    ): RewardedAdLoadListener = object : RewardedAdLoadListener {
        override fun onAdLoaded(loadedAd: RewardedAd) = onLoaded(loadedAd)
        override fun onAdFailedToLoad(error: AdRequestError) = onError(error)
    }

    override fun createAdEventListener(
        onShown: () -> Unit,
        onDismissed: () -> Unit,
        onFailedToShow: (com.yandex.mobile.ads.common.AdError) -> Unit,
        onClicked: () -> Unit,
        onImpression: (ImpressionData?) -> Unit,
        onRewarded: (Reward) -> Unit // Used for Rewarded
    ): RewardedAdEventListener = object : RewardedAdEventListener {
        override fun onAdShown() = onShown()
        override fun onAdDismissed() = onDismissed()
        override fun onAdFailedToShow(error: com.yandex.mobile.ads.common.AdError) =
            onFailedToShow(error)

        override fun onAdClicked() = onClicked()
        override fun onAdImpression(data: ImpressionData?) = onImpression(data)
        override fun onRewarded(reward: Reward) = onRewarded(reward)
    }
}
