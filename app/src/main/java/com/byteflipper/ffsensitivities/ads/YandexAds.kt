package com.byteflipper.ffsensitivities.ads

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            BannerAdView(context).apply {
                setAdUnitId(adUnitId)
                setAdSize(BannerAdSize.fixedSize(context, 320, 50))
                setBannerAdEventListener(object : BannerAdEventListener {
                    override fun onAdLoaded() {}
                    override fun onAdFailedToLoad(error: AdRequestError) {}
                    override fun onAdClicked() {}
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

class InterstitialAdManager(private val context: Activity) {
    private var interstitialAd: InterstitialAd? = null
    private var interstitialAdLoader: InterstitialAdLoader? = null

    fun loadAd(
        adUnitId: String,
        onLoaded: () -> Unit = {},
        onError: (AdRequestError) -> Unit = {},
        onShown: () -> Unit = {},
        onDismissed: () -> Unit = {}
    ) {
        interstitialAdLoader = InterstitialAdLoader(context).apply {
            setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    ad.setAdEventListener(object : InterstitialAdEventListener {
                        override fun onAdShown() { onShown() }
                        override fun onAdDismissed() {
                            onDismissed()
                            interstitialAd?.setAdEventListener(null)
                            interstitialAd = null
                        }
                        override fun onAdClicked() {}
                        override fun onAdImpression(data: ImpressionData?) {}
                        override fun onAdFailedToShow(error: com.yandex.mobile.ads.common.AdError) {}
                    })
                    onLoaded()
                }
                override fun onAdFailedToLoad(error: AdRequestError) {
                    onError(error)
                }
            })
        }

        val adRequest = AdRequestConfiguration.Builder(adUnitId).build()
        interstitialAdLoader?.loadAd(adRequest)
    }

    fun show() {
        interstitialAd?.show(context)
    }

    fun destroy() {
        interstitialAdLoader?.setAdLoadListener(null)
        interstitialAdLoader = null
        interstitialAd?.setAdEventListener(null)
        interstitialAd = null
    }
}

class RewardedAdManager(private val context: Activity) {
    private var rewardedAd: RewardedAd? = null
    private var rewardedAdLoader: RewardedAdLoader? = null

    fun loadAd(
        adUnitId: String,
        onLoaded: () -> Unit = {},
        onError: (AdRequestError) -> Unit = {},
        onRewarded: (Reward) -> Unit = {},
        onDismissed: () -> Unit = {}
    ) {
        rewardedAdLoader = RewardedAdLoader(context).apply {
            setAdLoadListener(object : RewardedAdLoadListener {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    ad.setAdEventListener(object : RewardedAdEventListener {
                        override fun onRewarded(reward: Reward) { onRewarded(reward) }
                        override fun onAdDismissed() {
                            onDismissed()
                            rewardedAd?.setAdEventListener(null)
                            rewardedAd = null
                        }
                        override fun onAdShown() {}
                        override fun onAdClicked() {}
                        override fun onAdImpression(data: ImpressionData?) {}
                        override fun onAdFailedToShow(error: com.yandex.mobile.ads.common.AdError) {}
                    })
                    onLoaded()
                }
                override fun onAdFailedToLoad(error: AdRequestError) {
                    onError(error)
                }
            })
        }

        val adRequest = AdRequestConfiguration.Builder(adUnitId).build()
        rewardedAdLoader?.loadAd(adRequest)
    }

    fun show() {
        rewardedAd?.show(context)
    }

    fun destroy() {
        rewardedAdLoader?.setAdLoadListener(null)
        rewardedAdLoader = null
        rewardedAd?.setAdEventListener(null)
        rewardedAd = null
    }
}

@Composable
fun ExampleScreen() {
    val context = LocalContext.current as Activity
    val interstitialAdManager = remember { InterstitialAdManager(context) }
    val rewardedAdManager = remember { RewardedAdManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            interstitialAdManager.destroy()
            rewardedAdManager.destroy()
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        YandexBannerAd(
            adUnitId = "your-banner-ad-unit-id",
            modifier = Modifier.fillMaxWidth()
        )

        LaunchedEffect(Unit) {
            interstitialAdManager.loadAd(
                adUnitId = "your-interstitial-ad-unit-id",
                onLoaded = { /* Ad loaded successfully */ },
                onError = { /* Handle error */ },
                onShown = { /* Ad shown */ },
                onDismissed = { /* Ad dismissed */ }
            )
        }

        LaunchedEffect(Unit) {
            rewardedAdManager.loadAd(
                adUnitId = "your-rewarded-ad-unit-id",
                onLoaded = { /* Ad loaded successfully */ },
                onError = { /* Handle error */ },
                onRewarded = { reward -> /* Handle reward */ },
                onDismissed = { /* Ad dismissed */ }
            )
        }
    }
}