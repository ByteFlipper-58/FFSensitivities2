// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    kotlin("plugin.serialization") version "1.8.20"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    id("com.google.dagger.hilt.android") version "2.52" apply false
    alias(libs.plugins.android.library) apply false
}

buildscript {
    dependencies {
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.2")
    }
}