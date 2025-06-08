package com.byteflipper.ui_components.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Универсальная карточка с иконкой, заголовком и пользовательским содержимым
 *
 * @param title заголовок карточки
 * @param icon иконка для карточки
 * @param modifier модификатор для стилизации
 * @param subtitle подзаголовок (опционально)
 * @param isElevated использовать ElevatedCard вместо обычной Card
 * @param colors цвета карточки
 * @param content пользовательское содержимое карточки
 */
@Composable
fun IconCard(
    title: String,
    icon: Painter,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    isElevated: Boolean = false,
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable () -> Unit = {}
) {
    if (isElevated) {
        ElevatedCard(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = colors.containerColor
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            IconCardContent(
                title = title,
                icon = icon,
                subtitle = subtitle,
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = colors,
            shape = RoundedCornerShape(16.dp)
        ) {
            IconCardContent(
                title = title,
                icon = icon,
                subtitle = subtitle,
                content = content
            )
        }
    }
}

@Composable
private fun IconCardContent(
    title: String,
    icon: Painter,
    subtitle: String?,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        if (content != {}) {
            Spacer(modifier = Modifier.size(16.dp))
            content()
        }
    }
} 