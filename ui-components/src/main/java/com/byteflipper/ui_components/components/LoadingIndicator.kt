package com.byteflipper.ui_components.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A custom loading indicator composable with rotating inner and outer circles.
 *
 * @param modifier Modifier for the Box container.
 * @param size The overall size of the indicator.
 * @param outerCircleStrokeWidth The stroke width of the outer circle.
 * @param innerCircleStrokeWidth The stroke width of the inner rotating circle.
 * @param centerDotSize The size of the center dot.
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 70.dp,
    outerCircleStrokeWidth: Dp = 2.5.dp,
    innerCircleStrokeWidth: Dp = 3.5.dp,
    centerDotSize: Dp = 15.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading_rotation_animation" // Changed label for clarity
    )

    // Calculate inner circle size based on overall size and stroke widths
    val innerCircleSize = size - (outerCircleStrokeWidth * 2) - (innerCircleStrokeWidth * 2) + 4.dp // Adjust slightly for visual balance

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .wrapContentSize()
            .size(size) // Use the size parameter
    ) {
        // Outer circle (track)
        CircularProgressIndicator(
            progress = { 1f }, // Make it a full circle
            modifier = Modifier.size(size), // Use the size parameter
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
            strokeWidth = outerCircleStrokeWidth, // Use parameter
            trackColor = Color.Transparent // Make track transparent
        )

        // Inner rotating circle
        CircularProgressIndicator(
            modifier = Modifier
                .size(innerCircleSize) // Adjusted size
                .rotate(rotation),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = innerCircleStrokeWidth, // Use parameter
            strokeCap = StrokeCap.Round,
            trackColor = Color.Transparent // Make track transparent
        )

        // Center dot
        Box(
            modifier = Modifier
                .size(centerDotSize) // Use parameter
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
        )
    }
}
