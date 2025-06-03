package com.byteflipper.ffsensitivities.utils

import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable

/**
 * Утилиты для оптимизации производительности LazyColumn и LazyVerticalGrid
 */
object LazyListUtils {
    
    /**
     * Оптимизированная функция для отображения элементов shimmer в LazyVerticalGrid
     */
    fun LazyGridScope.shimmerItems(
        count: Int,
        shimmerContent: @Composable () -> Unit
    ) {
        items(
            count = count,
            key = { index -> "shimmer_$index" },
            contentType = { "shimmer" }
        ) {
            shimmerContent()
        }
    }

    /**
     * Оптимизированная функция для отображения списка элементов в LazyVerticalGrid
     */
    inline fun <T> LazyGridScope.optimizedItems(
        items: List<T>,
        crossinline key: (item: T) -> Any,
        contentType: String = "default",
        crossinline itemContent: @Composable (item: T) -> Unit
    ) {
        items(
            items = items,
            key = { item -> key(item) },
            contentType = { contentType }
        ) { item ->
            itemContent(item)
        }
    }

    /**
     * Генерирует стабильный ключ для элементов состояния ошибки
     */
    fun generateErrorStateKey(errorType: String): String {
        return "error_state_$errorType"
    }

    /**
     * Константы для типов контента
     */
    object ContentTypes {
        const val SHIMMER = "shimmer"
        const val MANUFACTURER = "manufacturer"
        const val DEVICE = "device"
        const val ERROR_STATE = "error_state"
    }
} 