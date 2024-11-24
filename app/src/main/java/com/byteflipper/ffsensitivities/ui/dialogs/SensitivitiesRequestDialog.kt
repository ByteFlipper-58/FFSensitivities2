package com.byteflipper.ffsensitivities.ui.dialogs

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.byteflipper.ffsensitivities.PreferencesManager
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.SendSensitivitiesRequestMessageToBot
import com.google.android.gms.common.util.SharedPreferencesUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SensitivitiesRequestDialog(onDismiss: () -> Unit, onRequestSent: () -> Unit) {
    val context = LocalContext.current

    val infoBuffer = StringBuilder().apply {
        append("${context.getString(R.string.request_sensitivities_settings_message)}\n")
        append("-------------------------------------\n")
        append("Model: ${Build.MODEL}\n")
        append("Device: ${Build.DEVICE}\n")
        append("Manufacturer: ${Build.MANUFACTURER}\n")
        append("-------------------------------------\n")
        append("\n${context.getString(R.string.request_sensitivities_settings_message_two)}")
    }

    val requestBuffer = StringBuilder().apply {
        append("Sensitivities Request\n\n")
        append("-------------------------------------\n")
        append("Model: ${Build.MODEL}\n")
        append("Device: ${Build.DEVICE}\n")
        append("Manufacturer: ${Build.MANUFACTURER}\n")
        append("-------------------------------------\n")
        append("\n#SettingsRequest #${Build.MANUFACTURER}")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = context.getString(R.string.request_sensitivities_settings_title)) },
        text = {
            Text(text = infoBuffer.toString())
        },
        confirmButton = {
            Button(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val sendMessageToBot = SendSensitivitiesRequestMessageToBot()
                    val result = sendMessageToBot.sendMessageToGroup(requestBuffer.toString()) {
                        onRequestSent()
                        Toast.makeText(context, "Запрос отправлен", Toast.LENGTH_SHORT).show()
                    }

                    Log.d("SendMessageResult", result)
                }
                onDismiss()
            }) {
                Text(text = context.getString(R.string.request_sensitivities_settings_button))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}

@Preview
@Composable
fun PreviewSensitivitiesRequestDialog() {
    SensitivitiesRequestDialog(
        onDismiss = {},
        onRequestSent = {}
    )
}