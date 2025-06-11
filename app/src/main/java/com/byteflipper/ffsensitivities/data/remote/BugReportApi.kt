package com.byteflipper.ffsensitivities.data.remote

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// API v1 models
@Serializable
data class ForwardRequest(val tag: String, val message: String)

@Serializable
data class FeedbackRequest(val name: String, val email: String, val text: String)

@Serializable
data class ApiResponse(val status: String, val message: String? = null)

private const val TAG = "BugReportApi"
private const val BASE_URL = "https://byteflipper-feedback-bot.onrender.com/api/v1"

// Create HTTP client
private fun createHttpClient(): HttpClient {
    return HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        engine {
            requestTimeout = 15_000
        }
    }
}

// Send feedback (general feedback endpoint)
suspend fun sendFeedback(name: String, email: String, text: String): Result<ApiResponse> {
    val client = createHttpClient()
    val payload = FeedbackRequest(name = name, email = email, text = text)
    
    return try {
        val response: HttpResponse = client.post("$BASE_URL/feedback") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        
        val responseBody: ApiResponse = response.body()
        Log.i(TAG, "Feedback response: $responseBody")
        
        if (response.status.isSuccess()) {
            Result.success(responseBody)
        } else {
            Result.failure(Exception("Failed to send feedback: ${response.status.value} - ${responseBody.message}"))
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error sending feedback", e)
        Result.failure(e)
    } finally {
        client.close()
    }
}

// Forward message (bug report)
suspend fun sendBugReport(tag: String, message: String): Result<ApiResponse> {
    // Проверяем ограничения API
    if (message.length > 4096) {
        Log.w(TAG, "Message too long: ${message.length} characters (max 4096)")
        return Result.failure(Exception("Message too long (${message.length} characters, maximum 4096)"))
    }
    
    if (tag.isBlank()) {
        Log.w(TAG, "Tag is blank")
        return Result.failure(Exception("Tag cannot be empty"))
    }
    
    val client = createHttpClient()
    val payload = ForwardRequest(tag = tag, message = message)
    
    Log.d(TAG, "Sending bug report. Tag: '$tag', Message length: ${message.length}")
    Log.d(TAG, "Request payload: $payload")
    Log.d(TAG, "Request URL: $BASE_URL/forward")
    
    return try {
        val response: HttpResponse = client.post("$BASE_URL/forward") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        
        val responseBody = try {
            response.body<ApiResponse>()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse response body", e)
            // Попробуем получить raw response
            val rawBody = response.bodyAsText()
            Log.e(TAG, "Raw response body: $rawBody")
            ApiResponse(status = "error", message = "Failed to parse response: $rawBody")
        }
        
        Log.i(TAG, "Bug report response status: ${response.status.value}, Body: $responseBody")
        
        if (response.status.isSuccess()) {
            Result.success(responseBody)
        } else {
            val errorMessage = "Server error: ${response.status.value} - ${responseBody.message ?: "Unknown error"}"
            Log.e(TAG, errorMessage)
            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error sending bug report", e)
        Result.failure(e)
    } finally {
        client.close()
    }
}
