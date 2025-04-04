package com.byteflipper.ffsensitivities.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalAnimationApi::class) // Needed for AnimatedVisibility transitions
@Composable
fun OnboardingBottomBar(
    modifier: Modifier = Modifier,
    totalScreens: Int,
    currentScreenIndex: Int,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    onFinishClick: () -> Unit,
    backEnabled: Boolean = true,
    nextEnabled: Boolean = true,
    finishEnabled: Boolean = false // Typically enabled only on the last screen
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp), // Increased vertical padding
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalScreens) { index ->
                Indicator(isSelected = index == currentScreenIndex)
            }
        }

        // Navigation Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically // Align items vertically
        ) {
            // Animated Back Button (Smoother fade)
            AnimatedVisibility(
                visible = backEnabled && currentScreenIndex > 0, // Show only if enabled and not first screen
                enter = fadeIn(animationSpec = tween(200)), // Adjust duration if needed
                exit = fadeOut(animationSpec = tween(200))  // Adjust duration if needed
            ) {
                FilledIconButton(
                    onClick = onBackClick,
                    enabled = currentScreenIndex > 0, // Still needed for semantics
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back" // TODO: Add string resource
                    )
                }
            }
            // Add a spacer that appears when the back button is hidden to maintain layout
            AnimatedVisibility(
                visible = !backEnabled || currentScreenIndex == 0,
                enter = fadeIn(), // Optional: Animate spacer visibility if desired
                exit = fadeOut()
            ) {
                 Spacer(modifier = Modifier.size(48.dp)) // Keep spacing consistent
            }


            // Next/Finish Button
            FilledIconButton(
                onClick = if (finishEnabled) onFinishClick else onNextClick,
                enabled = nextEnabled || finishEnabled,
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                // Crossfade animation for the icon
                Crossfade(
                    targetState = finishEnabled,
                    animationSpec = tween(durationMillis = 300), // Adjust duration as needed
                    label = "NextFinishIconCrossfade"
                ) { isFinish ->
                    Icon(
                        imageVector = if (isFinish) Icons.Default.Done else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = if (isFinish) "Finish" else "Next" // TODO: Add string resource
                    )
                }
            }
        }
    }
}

@Composable
private fun Indicator(isSelected: Boolean) {
    // Jelly-like spring animation spec
    val springSpec = spring<Dp>(
        dampingRatio = Spring.DampingRatioMediumBouncy, // Adjust for more/less bounce
        stiffness = Spring.StiffnessLow // Adjust for faster/slower spring
    )
    val colorSpringSpec = spring<Color>(
         dampingRatio = Spring.DampingRatioMediumBouncy,
         stiffness = Spring.StiffnessLow
    )

    val width by animateDpAsState(
        targetValue = if (isSelected) 24.dp else 8.dp,
        animationSpec = springSpec, // Apply spring spec
        label = "IndicatorWidth"
    )
    val color by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        animationSpec = colorSpringSpec, // Apply spring spec
        label = "IndicatorColor"
    )
    Box(
        modifier = Modifier
            .height(8.dp)
            .width(width)
            .clip(CircleShape)
            .background(color)
    )
}
