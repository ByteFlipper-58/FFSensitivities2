package com.byteflipper.ffsensitivities.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun CompactSlider(
    labelResId: Int,
    initialValue: Float = 0f,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..200f,
    enabled: Boolean = true,
    trackHeight: Dp = 4.dp, // Thinner track
    thumbRadius: Dp = 8.dp // Smaller thumb
) {
    var sliderPosition by remember(initialValue, valueRange) {
        mutableFloatStateOf(initialValue.coerceIn(valueRange.start, valueRange.endInclusive))
    }
    val valueToRemember = if (enabled) sliderPosition else initialValue.coerceIn(valueRange.start, valueRange.endInclusive)

    val thumbRadiusPx = with(LocalDensity.current) { thumbRadius.toPx() }
    val trackHeightPx = with(LocalDensity.current) { trackHeight.toPx() }

    val activeColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    val inactiveColor = if (enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val thumbColor = activeColor

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp) // Keep some vertical padding for the whole component
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = labelResId),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = valueToRemember.roundToInt().toString(),
                modifier = Modifier.padding(start = 8.dp),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(thumbRadius * 2) // Height accommodates thumb
                .padding(top = 8.dp) // Padding between text and slider track
                .pointerInput(enabled, valueRange) {
                    if (!enabled) return@pointerInput

                    detectHorizontalDragGestures(
                        onDragStart = { /* Optional: Handle drag start */ },
                        onDragEnd = { /* Optional: Handle drag end */ },
                        onHorizontalDrag = { change, _ ->
                            val width = size.width
                            val dragPosition = change.position.x.coerceIn(0f, width.toFloat())
                            val rawValue = (dragPosition / width) * (valueRange.endInclusive - valueRange.start) + valueRange.start
                            val newValue = rawValue.coerceIn(valueRange.start, valueRange.endInclusive)

                            if (sliderPosition != newValue) {
                                sliderPosition = newValue
                                onValueChange(newValue)
                            }
                            change.consume()
                        }
                    )
                 }
         ) { // BoxWithConstraintsScope provides constraints
             // Calculations moved inside Canvas where size is available
             Canvas(modifier = Modifier.fillMaxSize()) { // DrawScope provides size
                 val width = size.width // Use Canvas's width
                 val sliderStart = thumbRadiusPx
                 val sliderEnd = width - thumbRadiusPx
                 val sliderWidth = (sliderEnd - sliderStart).coerceAtLeast(0f) // Ensure non-negative width

                 // Calculate thumb position based on current value
                 val valueFraction = if (valueRange.start == valueRange.endInclusive) 0f else
                     ((valueToRemember - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
                 val thumbX = sliderStart + valueFraction * sliderWidth
                 val thumbY = size.height / 2f // Center thumb vertically

                 // Draw inactive track
                drawLine(
                    color = inactiveColor,
                    start = Offset(sliderStart, thumbY),
                    end = Offset(sliderEnd, thumbY),
                    strokeWidth = trackHeightPx,
                    cap = StrokeCap.Round // Rounded ends for the track
                )

                 // Draw active track
                 if (sliderWidth > 0f) { // Check against float zero
                     drawLine(
                         color = activeColor,
                         start = Offset(sliderStart, thumbY),
                         end = Offset(thumbX.coerceIn(sliderStart, sliderEnd), thumbY), // Ensure active track doesn't go beyond bounds
                         strokeWidth = trackHeightPx,
                         cap = StrokeCap.Round
                     )
                 }

                 // Draw thumb
                 drawCircle(
                     color = thumbColor,
                     radius = thumbRadiusPx,
                     center = Offset(thumbX.coerceIn(sliderStart, sliderEnd), thumbY) // Ensure thumb stays within track bounds
                 )
             }
         }
    }
}
