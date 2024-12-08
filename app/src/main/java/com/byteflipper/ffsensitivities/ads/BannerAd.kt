package com.byteflipper.ffsensitivities.ads

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.byteflipper.ffsensitivities.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val bannerAdId = if (BuildConfig.DEBUG) BuildConfig.ADMOB_BANNER_TEST_AD_ID else BuildConfig.ADMOB_BANNER_AD_ID
    AndroidView(
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = bannerAdId
                loadAd(AdRequest.Builder().build())
            }
        },
        modifier = modifier.background(Color.Transparent)
    )
}