package com.byteflipper.ffsensitivities.ads

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.byteflipper.ffsensitivities.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BannerAdProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val consentManager: ConsentManager // Inject ConsentManager
) {

    // Check if ads can be shown based on consent
    private val canShowAds: Boolean
        get() = consentManager.canRequestPersonalizedAds()

    @Composable
    fun AdBanner(modifier: Modifier = Modifier, adUnitId: String? = null) {
        val actualAdUnitId = if (!adUnitId.isNullOrEmpty()) adUnitId else BuildConfig.ADMOB_BANNER_AD_ID
        val isInEditMode = LocalInspectionMode.current
        val displayMetrics = LocalContext.current.resources.displayMetrics
        val adWidthPixels = displayMetrics.widthPixels
        val density = displayMetrics.density
        val adWidthDp = (adWidthPixels / density).toInt()

        if (isInEditMode || !canShowAds) {
            if (isInEditMode) {
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ad Banner Preview")
                }
            }
            return
        }

        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(
                        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                            context,
                            adWidthDp
                        )
                    )
                    this.adUnitId = actualAdUnitId // Use the determined ad unit ID
                    this.loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
