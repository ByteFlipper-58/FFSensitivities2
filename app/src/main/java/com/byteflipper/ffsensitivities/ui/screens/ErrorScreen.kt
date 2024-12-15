package com.byteflipper.ffsensitivities.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ui.theme.FFSensitivitiesTheme

@Composable
fun ErrorScreen(
    errorMessage: String,
    onRetry: () -> Unit,
    onReportBug: () -> Unit,
    modifier: Modifier = Modifier
) {
    FFSensitivitiesTheme {
        ElevatedCard(
            modifier
                .fillMaxSize()
                .padding(14.dp, 8.dp, 14.dp, 8.dp),
            shape = ShapeDefaults.Large,
        ) {
            val clipboardManager = LocalClipboardManager.current

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = { onRetry() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 0.dp, 8.dp, 0.dp)
                ) {
                    Text(text = stringResource(R.string.try_again))
                }

                Button(
                    onClick = { onReportBug() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 0.dp, 8.dp, 0.dp)
                ) {
                    Text(text = stringResource(R.string.report_bug))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview() {
    ErrorScreen(
        errorMessage = "Произошла ошибка. Попробуйте снова.",
        onRetry = {},
        onReportBug = {}
    )
}
