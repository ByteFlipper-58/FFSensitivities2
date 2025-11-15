package com.byteflipper.ffsensitivities.data.remote

import android.util.Log
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service class for Bug Report API operations.
 * Delegates networking work to [BugReportApiClient] and exposes typed results.
 */
class BugReportApiService @Inject constructor(
    private val apiClient: BugReportApiClient
) {

    companion object {
        private const val TAG = "BugReportApiService"
    }

    suspend fun submitBugReport(
        tag: String,
        message: String
    ): SubmissionResult = withContext(Dispatchers.IO) {
        when (val result = apiClient.sendBugReport(tag, message)) {
            is ApiResult.Success -> {
                Log.i(TAG, "Bug report submitted successfully")
                SubmissionResult.Success(result.data)
            }
            is ApiResult.Error -> SubmissionResult.Failure(mapError(result.message, result.exception))
        }
    }

    suspend fun submitFeedback(
        name: String,
        email: String,
        text: String
    ): SubmissionResult = withContext(Dispatchers.IO) {
        when (val result = apiClient.sendFeedback(name, email, text)) {
            is ApiResult.Success -> SubmissionResult.Success(result.data)
            is ApiResult.Error -> SubmissionResult.Failure(mapError(result.message, result.exception))
        }
    }

    private fun mapError(message: String, exception: Exception?): SubmissionError {
        return when (exception) {
            is ValidationException -> SubmissionError.Validation(exception.message ?: message)
            is NetworkException -> SubmissionError.Network(exception.message ?: message)
            is ApiException -> SubmissionError.Server(exception.statusCode, exception.message ?: message)
            else -> SubmissionError.Unknown(message)
        }
    }
}

sealed class SubmissionResult {
    data class Success(val response: ApiResponse) : SubmissionResult()
    data class Failure(val error: SubmissionError) : SubmissionResult()
}

sealed class SubmissionError(open val reason: String) {
    data class Validation(override val reason: String) : SubmissionError(reason)
    data class Network(override val reason: String) : SubmissionError(reason)
    data class Server(val statusCode: Int?, override val reason: String) : SubmissionError(reason)
    data class Unknown(override val reason: String) : SubmissionError(reason)
}
