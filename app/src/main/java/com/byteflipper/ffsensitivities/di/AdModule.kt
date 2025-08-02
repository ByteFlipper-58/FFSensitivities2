package com.byteflipper.ffsensitivities.di

import android.content.Context
import com.byteflipper.ffsensitivities.ads.AdManager
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdModule {

    @Provides
    @Singleton
    fun provideAdManager(
        @ApplicationContext context: Context,
        @ApplicationScope coroutineScope: CoroutineScope,
        dataStoreManager: DataStoreManager
    ): AdManager {
        return AdManager(context, coroutineScope, dataStoreManager)
    }
} 