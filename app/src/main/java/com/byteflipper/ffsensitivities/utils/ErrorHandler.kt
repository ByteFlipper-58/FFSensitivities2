package com.byteflipper.ffsensitivities.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Централизованный обработчик ошибок для приложения
 */
@Singleton
class ErrorHandler @Inject constructor() {

    companion object {
        private const val TAG = "ErrorHandler"
    }

    /**
     * Обрабатывает исключение и возвращает соответствующее состояние ошибки
     */
    fun handleError(throwable: Throwable): ErrorState {
        // Логируем ошибку
        Log.e(TAG, "Handling error: ${throwable.message}", throwable)
        
        // Отправляем в Crashlytics (если не CancellationException)
        if (throwable !is CancellationException) {
            try {
                FirebaseCrashlytics.getInstance().recordException(throwable)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to record exception to Crashlytics", e)
            }
        }

        return when (throwable) {
            is CancellationException -> {
                // Не логируем отмены корутин как ошибки
                Log.d(TAG, "Operation cancelled: ${throwable.message}")
                ErrorState.OperationCancelled
            }
            is UnknownHostException, is IOException -> {
                Log.w(TAG, "Network error occurred", throwable)
                ErrorState.NetworkError
            }
            is SocketTimeoutException -> {
                Log.w(TAG, "Timeout error occurred", throwable)
                ErrorState.TimeoutError
            }
            is SecurityException -> {
                Log.w(TAG, "Permission error occurred", throwable)
                ErrorState.PermissionError
            }
            is IllegalArgumentException, is IllegalStateException -> {
                Log.e(TAG, "Invalid operation error", throwable)
                ErrorState.InvalidOperation(throwable.message ?: "Некорректная операция")
            }
            else -> {
                Log.e(TAG, "Unknown error occurred", throwable)
                ErrorState.GenericError(throwable.message ?: "Произошла неизвестная ошибка")
            }
        }
    }

    /**
     * Обрабатывает ошибку с дополнительным контекстом
     */
    fun handleError(throwable: Throwable, context: String): ErrorState {
        Log.e(TAG, "Error in context '$context': ${throwable.message}", throwable)
        
        // Добавляем контекст в Crashlytics
        if (throwable !is CancellationException) {
            try {
                FirebaseCrashlytics.getInstance().apply {
                    setCustomKey("error_context", context)
                    recordException(throwable)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to record exception with context to Crashlytics", e)
            }
        }
        
        return handleError(throwable)
    }

    /**
     * Получает пользовательское сообщение для ошибки
     */
    fun getUserMessage(errorState: ErrorState): String {
        return when (errorState) {
            is ErrorState.NetworkError -> "Проблема с подключением к интернету. Проверьте соединение и попробуйте снова."
            is ErrorState.TimeoutError -> "Превышено время ожидания. Попробуйте снова."
            is ErrorState.PermissionError -> "Недостаточно разрешений для выполнения операции."
            is ErrorState.InvalidOperation -> errorState.message
            is ErrorState.GenericError -> errorState.message
            is ErrorState.OperationCancelled -> "Операция отменена"
        }
    }
}

/**
 * Sealed класс для различных типов ошибок
 */
sealed class ErrorState {
    object NetworkError : ErrorState()
    object TimeoutError : ErrorState()
    object PermissionError : ErrorState()
    object OperationCancelled : ErrorState()
    data class InvalidOperation(val message: String) : ErrorState()
    data class GenericError(val message: String) : ErrorState()
} 