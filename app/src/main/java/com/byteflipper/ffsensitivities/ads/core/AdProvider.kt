package com.byteflipper.ffsensitivities.ads.core

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

/**
 * Базовый интерфейс для всех провайдеров рекламы
 */
interface AdProvider<T : AdState> {
    val adState: StateFlow<T>
    val config: AdConfig
    
    suspend fun load()
    suspend fun show(activity: Activity): AdResult
    fun isReady(): Boolean
    fun destroy()
}

/**
 * Провайдер для полноэкранной рекламы (интерстициальная, наградная, при открытии)
 */
interface FullScreenAdProvider<T : AdState> : AdProvider<T> {
    fun canShow(): Boolean
}

/**
 * Провайдер для баннерной рекламы
 */
interface BannerAdProvider : AdProvider<AdState> {
    fun createAdView(activity: Activity): android.view.View?
} 