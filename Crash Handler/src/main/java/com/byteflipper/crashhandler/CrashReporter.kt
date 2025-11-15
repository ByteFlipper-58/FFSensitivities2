package com.byteflipper.crashhandler

/**
 * Abstraction for crash reporting targets (e.g., Firebase Crashlytics).
 */
fun interface CrashReporter {
    fun recordException(throwable: Throwable)
}
