package com.byteflipper.crashhandler

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import java.io.PrintWriter
import java.io.StringWriter

class CrashHandlerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val crashInfo = intent.getStringExtra(EXTRA_CRASH_INFO) ?: "Unknown error occurred"
        val crashStackTrace = intent.getStringExtra(EXTRA_STACK_TRACE) ?: ""

        setContent {
            CrashHandlerTheme {
                CrashScreen(
                    errorMessage = crashInfo,
                    stackTrace = crashStackTrace,
                    onRestartClicked = { restartApp() },
                    onCloseClicked = { finishAffinity() }
                )
            }
        }
    }

    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finishAffinity()
    }

    companion object {
        private const val EXTRA_CRASH_INFO = "extra_crash_info"
        private const val EXTRA_STACK_TRACE = "extra_stack_trace"

        fun getIntent(context: Context, throwable: Throwable): Intent {
            return getIntent(context, throwable, CrashHandlerActivity::class.java)
        }

        fun getIntent(context: Context, throwable: Throwable, activityClass: Class<*>): Intent {
            val errorMessage = throwable.message ?: "Unknown error"
            val stackTraceWriter = StringWriter()
            throwable.printStackTrace(PrintWriter(stackTraceWriter))

            return Intent(context, activityClass).apply {
                putExtra(EXTRA_CRASH_INFO, errorMessage)
                putExtra(EXTRA_STACK_TRACE, stackTraceWriter.toString())
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        }
    }
}