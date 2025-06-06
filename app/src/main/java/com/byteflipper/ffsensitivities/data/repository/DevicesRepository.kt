package com.byteflipper.ffsensitivities.data.repository

import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import com.byteflipper.ffsensitivities.domain.model.ManufacturerWithModels
import com.byteflipper.ffsensitivities.presentation.ui.UiState
import com.byteflipper.ffsensitivities.utils.CacheManager
import com.byteflipper.ffsensitivities.utils.ErrorHandler
import com.byteflipper.ffsensitivities.utils.ErrorState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import android.util.Log
import io.ktor.http.contentType
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject

class DevicesRepository @Inject constructor(
    private val client: HttpClient,
    private val errorHandler: ErrorHandler,
    private val cacheManager: CacheManager
) {
    private companion object {
        private const val TAG = "DevicesRepository"
    }
    private val baseUrl = "https://raw.githubusercontent.com/ByteFlipper-58/database/refs/heads/main/FFSensitivities/"

    suspend fun fetchDevices(manufacturer: String): UiState<List<DeviceModel>> {
        val fileName = manufacturer.lowercase()
        val cacheKey = "devices_$fileName"
        
        Log.d(TAG, "Fetching devices for manufacturer: $manufacturer")
        
        // Проверяем кэш
        cacheManager.get(cacheKey) { jsonData ->
            Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }.decodeFromString<List<DeviceModel>>(jsonData)
        }?.let { cachedDevices ->
            Log.d(TAG, "Returning ${cachedDevices.size} cached devices for $manufacturer")
            return UiState.Success(cachedDevices)
        }
        
        // Если кэша нет, загружаем с сервера
        val url = "$baseUrl$fileName.json"
        Log.d(TAG, "Fetching devices from URL: $url")
        
        return try {
            val response: String = client.get(url) {
                contentType(ContentType.Application.Json)
            }.body()

            val manufacturerWithModels = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }.decodeFromString(ManufacturerWithModels.serializer(), response)

            val devices = manufacturerWithModels.models
            
            // Сохраняем в кэш
            cacheManager.put(cacheKey, devices) { deviceList ->
                Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                }.encodeToString<List<DeviceModel>>(deviceList)
            }
            
            Log.d(TAG, "Successfully fetched and cached ${devices.size} devices for $manufacturer")
            UiState.Success(devices)
            
        } catch (e: Exception) {
            val errorState = errorHandler.handleError(e, "fetchDevices for $manufacturer")
            
            return when (errorState) {
                is ErrorState.NetworkError -> UiState.NoInternet
                is ErrorState.TimeoutError -> UiState.Error("Превышено время ожидания. Попробуйте снова.")
                is ErrorState.GenericError -> UiState.Error(errorState.message)
                is ErrorState.InvalidOperation -> UiState.Error(errorState.message)
                is ErrorState.PermissionError -> UiState.Error("Недостаточно разрешений")
                is ErrorState.OperationCancelled -> UiState.Error("Операция отменена")
            }
        }
    }
}
