package com.byteflipper.ffsensitivities.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byteflipper.ffsensitivities.R

/**
 * Переиспользуемый компонент для отображения состояний ошибки
 */
@Composable
fun ErrorStateComponent(
    errorType: ErrorType,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = errorType.iconRes),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(errorType.titleRes),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(errorType.messageRes),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

/**
 * Типы ошибок с соответствующими иконками и текстами
 */
enum class ErrorType(
    val iconRes: Int,
    val titleRes: Int,
    val messageRes: Int
) {
    NO_INTERNET(
        iconRes = R.drawable.wifi_off_24px,
        titleRes = R.string.no_internet_connection,
        messageRes = R.string.check_internet_and_retry
    ),
    GENERAL_ERROR(
        iconRes = R.drawable.error_24px,
        titleRes = R.string.error_occurred,
        messageRes = R.string.try_again_later
    ),
    NETWORK_ERROR(
        iconRes = R.drawable.cloud_off_24px,
        titleRes = R.string.error_occurred,
        messageRes = R.string.check_internet_and_retry
    )
} 