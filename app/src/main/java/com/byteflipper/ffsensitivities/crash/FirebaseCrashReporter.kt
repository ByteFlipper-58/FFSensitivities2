package com.byteflipper.ffsensitivities.crash

import com.byteflipper.crashhandler.CrashReporter
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseCrashReporter(
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
) : CrashReporter {
    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
}
