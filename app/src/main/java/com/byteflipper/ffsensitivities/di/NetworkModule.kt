package com.byteflipper.ffsensitivities.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.byteflipper.ffsensitivities.data.remote.BugReportApiClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = false
        }
    }

    @GitHubClient
    @Provides
    @Singleton
    fun provideGitHubHttpClient(json: Json): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) { json(json) }
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 30_000
            }
            engine { maxConnectionsCount = 10 }
        }
    }

    @ApiClient
    @Provides
    @Singleton
    fun provideApiHttpClient(json: Json): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) { json(json) }
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 30_000
            }
            engine { maxConnectionsCount = 10 }
        }
    }

    @Provides
    @Singleton
    fun provideBugReportApiClient(@ApiClient client: HttpClient): BugReportApiClient {
        return BugReportApiClient(client)
    }
} 