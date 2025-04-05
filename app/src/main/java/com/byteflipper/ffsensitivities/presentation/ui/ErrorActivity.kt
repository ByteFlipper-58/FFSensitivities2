package com.byteflipper.ffsensitivities.presentation.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import com.byteflipper.ffsensitivities.presentation.ui.screens.ErrorScreen
import com.byteflipper.ffsensitivities.presentation.ui.theme.FFSensitivitiesTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ErrorActivity : ComponentActivity() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    companion object {
        const val EXTRA_ERROR_MESSAGE = "com.byteflipper.ffsensitivities.ERROR_MESSAGE"
        const val EXTRA_ERROR_STACKTRACE = "com.byteflipper.ffsensitivities.ERROR_STACKTRACE"

        fun newIntent(context: Context, message: String?, stackTrace: String?): Intent {
            return Intent(context, ErrorActivity::class.java).apply {
                putExtra(EXTRA_ERROR_MESSAGE, message)
                putExtra(EXTRA_ERROR_STACKTRACE, stackTrace)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val errorMessage = intent.getStringExtra(EXTRA_ERROR_MESSAGE)
        val stackTrace = intent.getStringExtra(EXTRA_ERROR_STACKTRACE)

        setContent {
            val themeSettingString by dataStoreManager.getTheme().collectAsState(initial = "system")
            val useDynamicColor by dataStoreManager.getDynamicColor().collectAsState(initial = true)
            val useContrast by dataStoreManager.getContrastTheme().collectAsState(initial = false)

            FFSensitivitiesTheme(
                themeSetting = themeSettingString,
                dynamicColorSetting = useDynamicColor,
                contrastThemeSetting = useContrast
            ) {
                ErrorScreen(
                    errorMessage = errorMessage,
                    stackTrace = stackTrace,
                    onRestartClick = { restartApplication() }
                )
            }
        }
    }

    private fun restartApplication() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
