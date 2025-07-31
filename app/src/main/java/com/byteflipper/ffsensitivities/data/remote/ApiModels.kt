package com.byteflipper.ffsensitivities.data.remote

import kotlinx.serialization.Serializable

// API v1 models
@Serializable
data class ForwardRequest(val tag: String, val message: String)

@Serializable
data class FeedbackRequest(val name: String, val email: String, val text: String)

@Serializable
data class ApiResponse(val status: String, val message: String? = null)

// Result wrapper for better error handling
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val exception: Exception? = null) : ApiResult<Nothing>()
}

// Custom exceptions
class ApiException(message: String, val statusCode: Int? = null) : Exception(message)
class ValidationException(message: String) : Exception(message)
class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause) 