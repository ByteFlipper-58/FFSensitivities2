package com.byteflipper.ffsensitivities.data.repository

import android.util.Log
import com.byteflipper.ffsensitivities.di.GitHubClient
import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import com.byteflipper.ffsensitivities.domain.model.ManufacturerWithModels
import com.byteflipper.ffsensitivities.utils.CacheManager
import com.byteflipper.ffsensitivities.utils.ErrorHandler
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class DevicesRepository @Inject constructor(
    @GitHubClient private val client: HttpClient,
    private val errorHandler: ErrorHandler,
    private val cacheManager: CacheManager
) {
    private companion object {
        private const val TAG = "DevicesRepository"
    }

    private val baseUrl = "https://raw.githubusercontent.com/ByteFlipper-58/database/refs/heads/main/FFSensitivities/"
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun fetchDevices(manufacturer: String): Result<List<DeviceModel>> {
        val fileName = manufacturer.lowercase()
        val cacheKey = "devices_$fileName"

        Log.d(TAG, "Fetching devices for manufacturer: $manufacturer")

        cacheManager.get(cacheKey) { jsonData ->
            json.decodeFromString(ListSerializer(DeviceModel.serializer()), jsonData)
        }?.let { cachedDevices ->
            Log.d(TAG, "Returning ${cachedDevices.size} cached devices for $manufacturer")
            return Result.success(cachedDevices)
        }

        val url = "$baseUrl$fileName.json"
        Log.d(TAG, "Fetching devices from URL: $url")

        return try {
            val response: String = client.get(url) {
                contentType(ContentType.Application.Json)
            }.body()

            val manufacturerWithModels = json.decodeFromString(ManufacturerWithModels.serializer(), response)
            val uniqueDevices = manufacturerWithModels.models.distinctBy { "${it.manufacturer}_${it.name}" }

            cacheManager.put(cacheKey, uniqueDevices) { deviceList ->
                json.encodeToString(deviceList)
            }

            Log.d(TAG, "Successfully fetched and cached ${uniqueDevices.size} devices for $manufacturer")
            Result.success(uniqueDevices)
        } catch (e: Exception) {
            errorHandler.handleError(e, "fetchDevices for $manufacturer")
            Result.failure(e)
        }
    }
}
