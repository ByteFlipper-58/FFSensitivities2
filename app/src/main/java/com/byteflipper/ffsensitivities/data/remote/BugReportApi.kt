package com.byteflipper.ffsensitivities.data.remote

import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Main API client for bug reports and feedback
 */
class BugReportApiClient {
    private val httpClient = HttpClientFactory.createHttpClient()
    
    /**
     * Send bug report with improved error handling and validation
     */
    suspend fun sendBugReport(tag: String, message: String): ApiResult<ApiResponse> {
        return withContext(Dispatchers.IO) {
            // Validation
            ValidationUtils.validateBugReport(tag, message).onFailure { 
                return@withContext ApiResult.Error(it.message ?: "Validation failed") 
            }
            
            val payload = ForwardRequest(tag = tag, message = message)
            ApiLogger.logRequest("${ApiConfig.BASE_URL}/forward", payload)
            
            withRetry {
                val response: HttpResponse = httpClient.post("${ApiConfig.BASE_URL}/forward") {
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                }
                
                ApiLogger.logResponse(response.status.value, "Processing response...")
                
                if (!response.status.isSuccess()) {
                    throw ApiException(
                        "HTTP ${response.status.value}",
                        response.status.value
                    )
                }
                
                try {
                    response.body<ApiResponse>()
                } catch (e: Exception) {
                    ApiLogger.logError("parsing response", e)
                    val rawBody = response.bodyAsText()
                    ApiLogger.error("Raw response body: $rawBody")
                    throw NetworkException("Failed to parse response: $rawBody", e)
                }
            }.fold(
                onSuccess = { 
                    ApiLogger.info("Bug report sent successfully: $it")
                    ApiResult.Success(it) 
                },
                onFailure = { 
                    ApiLogger.logError("sending bug report", it)
                    ApiResult.Error(it.message ?: "Unknown error", it as? Exception) 
                }
            )
        }
    }
    
    /**
     * Send feedback with improved error handling and validation
     */
    suspend fun sendFeedback(name: String, email: String, text: String): ApiResult<ApiResponse> {
        return withContext(Dispatchers.IO) {
            // Validation
            ValidationUtils.validateFeedback(name, email, text).onFailure { 
                return@withContext ApiResult.Error(it.message ?: "Validation failed") 
            }
            
            val payload = FeedbackRequest(name = name, email = email, text = text)
            ApiLogger.logRequest("${ApiConfig.BASE_URL}/feedback", payload)
            
            withRetry {
                val response: HttpResponse = httpClient.post("${ApiConfig.BASE_URL}/feedback") {
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                }
                
                ApiLogger.logResponse(response.status.value, "Processing response...")
                
                if (!response.status.isSuccess()) {
                    throw ApiException(
                        "HTTP ${response.status.value}",
                        response.status.value
                    )
                }
                
                try {
                    response.body<ApiResponse>()
                } catch (e: Exception) {
                    ApiLogger.logError("parsing response", e)
                    val rawBody = response.bodyAsText()
                    ApiLogger.error("Raw response body: $rawBody")
                    throw NetworkException("Failed to parse response: $rawBody", e)
                }
            }.fold(
                onSuccess = { 
                    ApiLogger.info("Feedback sent successfully: $it")
                    ApiResult.Success(it) 
                },
                onFailure = { 
                    ApiLogger.logError("sending feedback", it)
                    ApiResult.Error(it.message ?: "Unknown error", it as? Exception) 
                }
            )
        }
    }
    
    /**
     * Close the HTTP client
     */
    fun close() {
        httpClient.close()
    }
}