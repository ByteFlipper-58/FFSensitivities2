package com.byteflipper.crashhandler

import android.app.Application
import android.content.Context
import android.os.Process
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class CrashHandler private constructor() : Thread.UncaughtExceptionHandler {

    private var defaultExceptionHandler: Thread.UncaughtExceptionHandler? = null
    private lateinit var applicationContext: Context
    private var crashLogsDir: File? = null
    private var enableCrashActivity = true
    private var enableLogSaving = true
    private var customCrashActivityClass: Class<*>? = null
    private var crashReporter: CrashReporter? = null
    private var maxLogFiles: Int = 20
    private var maxLogRetentionMs: Long = TimeUnit.DAYS.toMillis(7)

    // Методы конфигурации на уровне экземпляра с возвратом this для цепочки
    fun configureCrashLogsDir(directory: File): CrashHandler {
        crashLogsDir = directory
        return this
    }

    fun enableCrashActivity(enable: Boolean): CrashHandler {
        enableCrashActivity = enable
        return this
    }

    fun enableLogSaving(enable: Boolean): CrashHandler {
        enableLogSaving = enable
        return this
    }

    fun setCustomCrashActivity(activityClass: Class<*>): CrashHandler {
        customCrashActivityClass = activityClass
        return this
    }

    fun setCrashReporter(reporter: CrashReporter?): CrashHandler {
        crashReporter = reporter
        return this
    }

    fun configureLogRetention(maxFiles: Int = 20, maxAgeDays: Int = 7): CrashHandler {
        maxLogFiles = maxFiles.coerceAtLeast(1)
        maxLogRetentionMs = TimeUnit.DAYS.toMillis(maxAgeDays.coerceAtLeast(1).toLong())
        return this
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            // Save crash log to file if enabled
            if (enableLogSaving) {
                saveCrashLog(throwable)
            }

            // Send crash to Firebase Crashlytics if enabled
            crashReporter?.recordException(throwable)

            // Start crash activity if enabled
            if (enableCrashActivity) {
                val crashActivityClass = customCrashActivityClass ?: CrashHandlerActivity::class.java
                applicationContext.startActivity(
                    CrashHandlerActivity.getIntent(applicationContext, throwable, crashActivityClass)
                )
            }
        } catch (e: Exception) {
            // If anything goes wrong with our handler, call the default one
            e.printStackTrace()
        }

        // If we have a default handler, call it
        defaultExceptionHandler?.uncaughtException(thread, throwable) ?: run {
            // Otherwise, terminate the app
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }

    private fun saveCrashLog(throwable: Throwable) {
        try {
            crashLogsDir?.let { dir ->
                if (!dir.exists()) {
                    dir.mkdirs()
                }

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val logFile = File(dir, "crash_$timestamp.txt")

                FileOutputStream(logFile).use { fos ->
                    fos.write("Crash Time: $timestamp\n\n".toByteArray())
                    fos.write("Exception: ${throwable.javaClass.name}\n".toByteArray())
                    fos.write("Message: ${throwable.message}\n\n".toByteArray())

                    val stackTraceWriter = StringWriter()
                    throwable.printStackTrace(PrintWriter(stackTraceWriter))
                    fos.write(stackTraceWriter.toString().toByteArray())
                }

                cleanupOldLogs(dir)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cleanupOldLogs(directory: File) {
        val files = directory.listFiles() ?: return
        val cutoff = System.currentTimeMillis() - maxLogRetentionMs

        files.filter { it.lastModified() < cutoff }.forEach { it.delete() }

        val remaining = directory.listFiles()?.sortedByDescending { it.lastModified() } ?: return
        if (remaining.size > maxLogFiles) {
            remaining.drop(maxLogFiles).forEach { it.delete() }
        }
    }

    companion object {
        @Volatile
        private var instance: CrashHandler? = null

        private fun getInstance(): CrashHandler {
            return instance ?: synchronized(this) {
                instance ?: CrashHandler().also { instance = it }
            }
        }

        /**
         * Инициализирует CrashHandler с контекстом приложения.
         * Этот метод должен быть вызван перед любыми другими методами конфигурации.
         *
         * @param application Контекст приложения
         * @return Экземпляр CrashHandler для цепочки методов
         */
        fun init(application: Application): CrashHandler {
            return getInstance().apply {
                applicationContext = application.applicationContext
                defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
                Thread.setDefaultUncaughtExceptionHandler(this)

                // Default crash logs directory
                crashLogsDir = File(application.getExternalFilesDir(null), "crash_logs")
            }
        }
    }
}
