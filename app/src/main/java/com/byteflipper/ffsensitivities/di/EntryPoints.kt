package com.byteflipper.ffsensitivities.di

import com.byteflipper.ffsensitivities.data.remote.BugReportApiClient
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NetworkEntryPoint {
    fun bugReportApiClient(): BugReportApiClient
}

