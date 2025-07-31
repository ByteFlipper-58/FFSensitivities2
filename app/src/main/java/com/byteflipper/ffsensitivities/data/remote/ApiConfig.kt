package com.byteflipper.ffsensitivities.data.remote

object ApiConfig {
    const val BASE_URL = "https://byteflipper-feedback-bot.onrender.com/api/v1"
    const val REQUEST_TIMEOUT = 15_000L
    const val MAX_MESSAGE_LENGTH = 4096
    const val TAG = "BugReportApi"
    const val MAX_RETRY_ATTEMPTS = 3
    const val RETRY_DELAY_MS = 1000L
} 