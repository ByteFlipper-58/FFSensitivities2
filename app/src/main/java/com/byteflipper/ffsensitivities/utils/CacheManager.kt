package com.byteflipper.ffsensitivities.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Простой менеджер кэширования для данных приложения
 */
@Singleton
class CacheManager @Inject constructor(
    private val context: Context,
    private val json: Json
) {
    companion object {
        private const val TAG = "CacheManager"
        private const val CACHE_DIR = "app_cache"
        private const val CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000L // 24 часа
    }

    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR).apply {
            if (!exists()) mkdirs()
        }
    }

    private val memoryCache = ConcurrentHashMap<String, CacheEntry>()
    private val fileMutex = Mutex()

    /**
     * Кэшированная запись с данными и временем создания
     */
    private data class CacheEntry(
        val data: String,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS
    }

    /**
     * Сохраняет данные в кэш (память + диск)
     */
    suspend fun <T> put(key: String, data: T, serializer: (T) -> String) {
        try {
            val jsonData = serializer(data)
            val entry = CacheEntry(jsonData)
            
            // Сохраняем в память
            memoryCache[key] = entry
            
            // Сохраняем на диск
            fileMutex.withLock {
                val file = File(cacheDir, "${key.hashCode()}.cache")
                file.writeText(jsonData)
            }
            
            Log.d(TAG, "Cached data for key: $key")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cache data for key: $key", e)
        }
    }

    /**
     * Получает данные из кэша (сначала память, потом диск)
     */
    suspend fun <T> get(key: String, deserializer: (String) -> T): T? {
        return try {
            // Проверяем память
            memoryCache[key]?.let { entry ->
                if (!entry.isExpired()) {
                    Log.d(TAG, "Cache hit in memory for key: $key")
                    return deserializer(entry.data)
                } else {
                    // Удаляем истекший кэш
                    memoryCache.remove(key)
                }
            }

            // Проверяем диск
            fileMutex.withLock {
                val file = File(cacheDir, "${key.hashCode()}.cache")
                if (file.exists()) {
                    val data = file.readText()
                    val entry = CacheEntry(data)
                    
                    if (!entry.isExpired()) {
                        // Восстанавливаем в память
                        memoryCache[key] = entry
                        Log.d(TAG, "Cache hit on disk for key: $key")
                        return@withLock deserializer(data)
                    } else {
                        // Удаляем истекший файл
                        file.delete()
                    }
                }
            }
            
            Log.d(TAG, "Cache miss for key: $key")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get cached data for key: $key", e)
            null
        }
    }

    /**
     * Проверяет существование валидного кэша
     */
    fun contains(key: String): Boolean {
        return memoryCache[key]?.let { !it.isExpired() } ?: run {
            val file = File(cacheDir, "${key.hashCode()}.cache")
            file.exists() && (System.currentTimeMillis() - file.lastModified()) < CACHE_EXPIRY_MS
        }
    }

    /**
     * Удаляет конкретную запись из кэша
     */
    suspend fun remove(key: String) {
        memoryCache.remove(key)
        fileMutex.withLock {
            val file = File(cacheDir, "${key.hashCode()}.cache")
            if (file.exists()) {
                file.delete()
                Log.d(TAG, "Removed cache for key: $key")
            }
        }
    }

    /**
     * Очищает весь кэш
     */
    suspend fun clear() {
        memoryCache.clear()
        fileMutex.withLock {
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".cache")) {
                    file.delete()
                }
            }
            Log.d(TAG, "Cleared all cache")
        }
    }

    /**
     * Очищает истекший кэш
     */
    suspend fun clearExpired() {
        // Очищаем память
        val expiredKeys = memoryCache.filter { it.value.isExpired() }.keys
        expiredKeys.forEach { memoryCache.remove(it) }

        // Очищаем диск
        fileMutex.withLock {
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".cache") && 
                    (System.currentTimeMillis() - file.lastModified()) > CACHE_EXPIRY_MS) {
                    file.delete()
                }
            }
        }
        
        if (expiredKeys.isNotEmpty()) {
            Log.d(TAG, "Cleared ${expiredKeys.size} expired cache entries")
        }
    }

    /**
     * Получает размер кэша в байтах
     */
    fun getCacheSize(): Long {
        return cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
    }
} 