package com.byteflipper.ffsensitivities.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GitHubClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiClient

