package com.byteflipper.ffsensitivities.ads.core

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError

/**
 * Базовый интерфейс для состояний рекламы
 */
sealed interface AdState {
    data object Initial : AdState
    data object Loading : AdState
    data object Loaded : AdState
    data object Showing : AdState
    data object Dismissed : AdState
    data class LoadFailed(val error: LoadAdError) : AdState
    data class ShowFailed(val error: AdError) : AdState
}

/**
 * Расширенное состояние для наградной рекламы
 */
sealed interface RewardedAdState : AdState {
    data object Initial : RewardedAdState
    data object Loading : RewardedAdState
    data object Loaded : RewardedAdState
    data object Showing : RewardedAdState
    data object Dismissed : RewardedAdState
    data class LoadFailed(val error: String) : RewardedAdState
    data class ShowFailed(val error: String) : RewardedAdState
    data class RewardEarned(val reward: AdReward) : RewardedAdState
}

/**
 * Данные о награде
 */
data class AdReward(
    val amount: Int,
    val type: String
)

/**
 * Данные о результате показа рекламы
 */
data class AdResult(
    val adType: AdType,
    val success: Boolean,
    val reward: com.google.android.gms.ads.rewarded.RewardItem? = null,
    val error: Throwable? = null
) 