package com.byteflipper.ffsensitivities.data.remote

import kotlinx.coroutines.delay

/**
 * Retry mechanism for API calls
 */
suspend fun <T> withRetry(
    maxAttempts: Int = ApiConfig.MAX_RETRY_ATTEMPTS,
    delayMs: Long = ApiConfig.RETRY_DELAY_MS,
    block: suspend () -> T
): Result<T> {
    repeat(maxAttempts) { attempt ->
        try {
            return Result.success(block())
        } catch (e: Exception) {
            ApiLogger.warning("Attempt ${attempt + 1} failed: ${e.message}")
            if (attempt == maxAttempts - 1) {
                return Result.failure(e)
            }
            delay(delayMs * (attempt + 1))
        }
    }
    return Result.failure(Exception("Max attempts reached"))
}

/**
 * Validation utilities
 */
object ValidationUtils {
    fun validateBugReport(tag: String, message: String): Result<Unit> {
        return when {
            tag.isBlank() -> Result.failure(ValidationException("Tag cannot be empty"))
            message.length > ApiConfig.MAX_MESSAGE_LENGTH -> {
                Result.failure(ValidationException(
                    "Message too long (${message.length} characters, maximum ${ApiConfig.MAX_MESSAGE_LENGTH})"
                ))
            }
            else -> Result.success(Unit)
        }
    }
    
    fun validateFeedback(name: String, email: String, text: String): Result<Unit> {
        return when {
            name.isBlank() -> Result.failure(ValidationException("Name cannot be empty"))
            email.isBlank() -> Result.failure(ValidationException("Email cannot be empty"))
            !email.contains("@") -> Result.failure(ValidationException("Invalid email format"))
            text.isBlank() -> Result.failure(ValidationException("Text cannot be empty"))
            else -> Result.success(Unit)
        }
    }
} 