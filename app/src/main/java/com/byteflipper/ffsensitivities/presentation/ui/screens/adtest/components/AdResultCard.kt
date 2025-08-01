package com.byteflipper.ffsensitivities.presentation.ui.screens.adtest.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ads.core.AdResult

@Composable
fun AdResultCard(result: AdResult) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (result.success)
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                else
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (result.success) painterResource(id = R.drawable.check_circle_24px) else painterResource(id = R.drawable.error_24px),
                        contentDescription = null,
                        tint = if (result.success)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "📋 Последний результат",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                InfoChip(label = "Тип", value = result.adType.toString())
                Spacer(modifier = Modifier.height(8.dp))
                InfoChip(
                    label = "Статус",
                    value = if (result.success) "Успешно" else "Ошибка",
                    isSuccess = result.success
                )

                result.error?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoChip(
                        label = "Ошибка",
                        value = error.message ?: "Неизвестная ошибка",
                        isError = true
                    )
                }

                result.reward?.let { reward ->
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoChip(
                        label = "Награда",
                        value = "${reward.amount} ${reward.type}",
                        isReward = true
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String,
    isSuccess: Boolean = false,
    isError: Boolean = false,
    isReward: Boolean = false
) {
    val backgroundColor = when {
        isSuccess -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        isError -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        isReward -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, MaterialTheme.shapes.small)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = when {
                isSuccess -> MaterialTheme.colorScheme.primary
                isError -> MaterialTheme.colorScheme.error
                isReward -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
} 