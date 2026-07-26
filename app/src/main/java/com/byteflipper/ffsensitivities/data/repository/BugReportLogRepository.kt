package com.byteflipper.ffsensitivities.data.repository

import android.content.Context
import com.byteflipper.ffsensitivities.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BugReportLogRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun readLogFileContent(): String {
        return try {
            readLegacyLogFile()
                ?: readLatestCrashLog()
                ?: context.getString(R.string.bug_report_log_not_found)
        } catch (e: IOException) {
            context.getString(
                R.string.bug_report_log_read_error,
                e.message ?: context.getString(R.string.unknown_error)
            )
        }
    }

    private fun readLegacyLogFile(): String? {
        val logDir = File(context.getExternalFilesDir(null), "logs")
        val logFile = File(logDir, "logs.txt")
        return logFile.takeIf { it.exists() }?.readText()
    }

    private fun readLatestCrashLog(): String? {
        val logsDir = File(context.getExternalFilesDir(null), "my_crash_logs")
        val latestLog = logsDir
            .takeIf { it.exists() && it.isDirectory }
            ?.listFiles()
            ?.filter { it.isFile }
            ?.maxByOrNull { it.lastModified() }

        return latestLog?.readText()
    }
}
