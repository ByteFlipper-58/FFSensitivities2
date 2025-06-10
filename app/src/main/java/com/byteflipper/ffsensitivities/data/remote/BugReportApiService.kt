package com.byteflipper.ffsensitivities.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service class for Bug Report API operations
 * Provides a centralized way to interact with the feedback API
 */
class BugReportApiService {
    
    companion object {
        private const val TAG = "BugReportApiService"
        
        /**
         * Test API connection with a simple message
         */
        suspend fun testApiConnection(): Result<ApiResponse> = withContext(Dispatchers.IO) {
            return@withContext try {
                Log.i(TAG, "Testing API connection...")
                val result = sendBugReport("other", "API connection test")
                if (result.isSuccess) {
                    Log.i(TAG, "API connection test successful")
                } else {
                    Log.e(TAG, "API connection test failed: ${result.exceptionOrNull()?.message}")
                }
                result
            } catch (e: Exception) {
                Log.e(TAG, "API connection test error", e)
                Result.failure(e)
            }
        }
        
        /**
         * Submit a bug report with automatic retry logic
         */
        suspend fun submitBugReport(
            tag: String, 
            message: String,
            maxRetries: Int = 2
        ): Result<ApiResponse> = withContext(Dispatchers.IO) {
            
            var lastException: Exception? = null
            
            repeat(maxRetries) { attempt ->
                try {
                    Log.i(TAG, "Attempting to send bug report (attempt ${attempt + 1}/$maxRetries)")
                    val result = sendBugReport(tag, message)
                    if (result.isSuccess) {
                        Log.i(TAG, "Bug report submitted successfully on attempt ${attempt + 1}")
                        return@withContext result
                    } else {
                        lastException = result.exceptionOrNull() as? Exception
                        Log.w(TAG, "Bug report submission failed on attempt ${attempt + 1}: ${lastException?.message}")
                    }
                } catch (e: Exception) {
                    lastException = e
                    Log.w(TAG, "Bug report submission attempt ${attempt + 1} failed", e)
                }
                
                // Wait before retry (but not after last attempt)
                if (attempt < maxRetries - 1) {
                    Log.d(TAG, "Waiting before retry...")
                    kotlinx.coroutines.delay(2000L)
                }
            }
            
            Log.e(TAG, "All $maxRetries attempts failed")
            Result.failure(lastException ?: Exception("Не удалось отправить запрос после $maxRetries попыток"))
        }
        
        /**
         * Submit general feedback (not categorized bug report)
         */
        suspend fun submitFeedback(
            name: String,
            email: String,
            text: String
        ): Result<ApiResponse> = withContext(Dispatchers.IO) {
            return@withContext try {
                sendFeedback(name, email, text)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to submit feedback", e)
                Result.failure(e)
            }
        }
        
        /**
         * Test different tags to see which ones work
         */
        suspend fun testApiTags(): Result<String> = withContext(Dispatchers.IO) {
            val testMessage = "Тестовое сообщение для проверки тега"
            val tags = listOf("other", "support", "bug_report", "settings_request", "feature_request")
            val results = mutableListOf<String>()
            
            for (tag in tags) {
                try {
                    Log.i(TAG, "Testing tag: $tag")
                    val result = sendBugReport(tag, "$testMessage ($tag)")
                    if (result.isSuccess) {
                        results.add("✅ $tag: работает")
                        Log.i(TAG, "Tag $tag works")
                    } else {
                        val error = result.exceptionOrNull()?.message ?: "Неизвестная ошибка"
                        results.add("❌ $tag: $error")
                        Log.w(TAG, "Tag $tag failed: $error")
                    }
                } catch (e: Exception) {
                    results.add("❌ $tag: ${e.message}")
                    Log.e(TAG, "Tag $tag error", e)
                }
                
                // Small delay between tests
                kotlinx.coroutines.delay(1000)
            }
            
            val summary = results.joinToString("\n")
            Log.i(TAG, "Tag test summary:\n$summary")
            return@withContext Result.success(summary)
        }
    }
} 