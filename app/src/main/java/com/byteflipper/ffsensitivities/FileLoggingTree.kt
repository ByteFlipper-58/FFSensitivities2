package com.byteflipper.ffsensitivities

import android.content.Context
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter

class FileLoggingTree(private val context: Context) : Timber.Tree() {

    private val logFile: File by lazy {
        val dir = File(context.getExternalFilesDir(null), "logs")
        if (!dir.exists()) dir.mkdirs()
        File(dir, "logs.txt")
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        try {
            val logMessage = "${System.currentTimeMillis()} $priority/$tag: $message\n"
            FileWriter(logFile, true).use { writer ->
                writer.append(logMessage)
            }
            t?.let {
                FileWriter(logFile, true).use { writer ->
                    writer.append("Exception: ${it.message}\n")
                    it.printStackTrace(PrintWriter(writer))
                }
            }
        } catch (e: IOException) {
            Timber.e(e, "Failed to log to file")
        }
    }
}
