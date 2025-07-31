package com.byteflipper.ffsensitivities.data.remote

import android.util.Log

object ApiLogger {
    private const val TAG = "BugReportApi"
    
    fun debug(message: String) = Log.d(TAG, message)
    fun info(message: String) = Log.i(TAG, message)
    fun warning(message: String) = Log.w(TAG, message)
    fun error(message: String, throwable: Throwable? = null) = Log.e(TAG, message, throwable)
    
    fun logRequest(url: String, payload: Any) {
        debug("Request URL: $url")
        debug("Request payload: $payload")
    }
    
    fun logResponse(statusCode: Int, responseBody: Any) {
        info("Response status: $statusCode, Body: $responseBody")
    }
    
    fun logError(operation: String, error: Throwable) {
        error("Error during $operation", error)
    }
} 