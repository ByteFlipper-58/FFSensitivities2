package com.byteflipper.ffsensitivities.service

import com.byteflipper.ffsensitivities.data.ManufacturerWithModels
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface DeviceApiService {
    @GET("{model}.json")
    suspend fun getDevicesForModel(@Path("model") model: String): ManufacturerWithModels
}

object DevicesRetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/ByteFlipper-58/FFSensitivities/refs/heads/master/app/src/main/assets/sensitivity_settings/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: DeviceApiService by lazy {
        retrofit.create(DeviceApiService::class.java)
    }
}
