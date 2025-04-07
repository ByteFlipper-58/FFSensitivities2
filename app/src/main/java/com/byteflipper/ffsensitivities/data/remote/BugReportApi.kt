package com.byteflipper.ffsensitivities.data.remote

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber

@Serializable
data class RequestPayload(val tag: String, val message: String)

@Serializable
data class ResponseData(val status: String? = null, val message: String? = null, val error: String? = null)

suspend fun sendBugReport(tag: String, message: String, serverUrl: String = "https://byteflipper-feedback-bot.onrender.com/forward"): Result<ResponseData> {
    val client = HttpClient(CIO) {
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

    val payload = RequestPayload(tag = tag, message = message)
    Timber.d("Sending bug report. Tag: $tag, Payload size: ${message.length}")

    return try {
        val response: HttpResponse = client.post(serverUrl) {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }

        val responseBody: ResponseData = response.body()
        Timber.i("Bug report response status: ${response.status}, Body: $responseBody")

        if (response.status.isSuccess()) {
            Result.success(responseBody)
        } else {
            Result.failure(Exception("Server error: ${response.status.value} - ${responseBody.error ?: responseBody.message ?: "Unknown error"}"))
        }
    } catch (e: Exception) {
        Timber.e(e, "Error sending bug report")
        Result.failure(e)
    } finally {
        client.close()
    }
}
