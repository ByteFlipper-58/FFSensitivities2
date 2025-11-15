package com.byteflipper.ffsensitivities.ads.core

/**
 * Abstraction used by ad providers to check whether personalized ads can be requested.
 */
interface AdConsentProvider {
    fun canRequestPersonalizedAds(): Boolean
}
