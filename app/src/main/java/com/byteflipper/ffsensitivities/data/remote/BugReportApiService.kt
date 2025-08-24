package com.byteflipper.ffsensitivities.data.remote

import android.util.Log
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service class for Bug Report API operations
 * Provides a centralized way to interact with the feedback API
 */
class BugReportApiService @Inject constructor(
    private val apiClient: BugReportApiClient
) {
    companion object {
        private const val TAG = "BugReportApiService"
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
                val result = apiClient.sendBugReport(tag, message)

                when (result) {
                    is ApiResult.Success -> {
                        Log.i(
                            TAG,
                            "Bug report submitted successfully on attempt ${attempt + 1}"
                        )
                        return@withContext Result.success(result.data)
                    }

                    is ApiResult.Error -> {
                        lastException = result.exception ?: Exception(result.message)
                        Log.w(
                            TAG,
                            "Bug report submission failed on attempt ${attempt + 1}: ${lastException?.message}"
                        )
                    }
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
        Result.failure(
            lastException ?: Exception("Не удалось отправить запрос после $maxRetries попыток")
        )
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
            val result = apiClient.sendFeedback(name, email, text)

            when (result) {
                is ApiResult.Success -> Result.success(result.data)
                is ApiResult.Error -> Result.failure(
                    result.exception ?: Exception(result.message)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit feedback", e)
            Result.failure(e)
        }
    }
}