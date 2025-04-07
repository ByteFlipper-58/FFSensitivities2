package com.byteflipper.ffsensitivities.presentation.ui.screens.bugreport.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byteflipper.ffsensitivities.R
import kotlinx.coroutines.delay

@Composable
fun SubmissionStatusOverlay(
    isSubmitting: Boolean,
    isSuccess: Boolean,
    submissionError: String?,
    onRetry: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = isSubmitting,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.7f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Loading Indicator / Error Message Card
                AnimatedVisibility(
                    visible = !isSuccess,
                    enter = fadeIn(animationSpec = tween(500)) +
                            slideInVertically(
                                initialOffsetY = { -it / 2 },
                                animationSpec = tween(500, easing = FastOutSlowInEasing)
                            ),
                    exit = fadeOut(animationSpec = tween(300)) +
                            slideOutVertically(
                                targetOffsetY = { -it / 2 },
                                animationSpec = tween(300)
                            )
                ) {
                    LoadingOrErrorCard(
                        isLoading = submissionError == null,
                        errorMessage = submissionError,
                        onRetry = onRetry,
                        onDismiss = onDismiss
                    )
                }

                // Success Message Card
                AnimatedVisibility(
                    visible = isSuccess && submissionError == null,
                    enter = fadeIn(animationSpec = tween(500)) +
                            expandVertically(
                                expandFrom = Alignment.Top,
                                animationSpec = tween(500, easing = FastOutSlowInEasing)
                            ),
                    exit = fadeOut(animationSpec = tween(300)) +
                            shrinkVertically(
                                shrinkTowards = Alignment.Top,
                                animationSpec = tween(300)
                            )
                ) {
                    SuccessCard(onDismiss = onDismiss)
                }
            }
        }
    }
}

@Composable
private fun LoadingOrErrorCard(
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(0.85f)
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                if (isLoading) {
                    // --- Loading State ---
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LoadingIndicator()
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = stringResource(id = R.string.bug_report_submitting_title), // Use string resource
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(id = R.string.bug_report_submitting_subtitle), // Use string resource
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // --- Error State ---
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PulsingErrorIcon()
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(id = R.string.bug_report_submission_error_title), // Use string resource
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        // Use string resource, provide default if errorMessage is null
                        text = errorMessage ?: stringResource(id = R.string.bug_report_submission_failed_default),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(id = R.string.bug_report_action_cancel), // Use string resource
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = onRetry,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = stringResource(id = R.string.bug_report_action_retry), // Use string resource
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading_rotation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .wrapContentSize()
            .size(70.dp)
    ) {
        // Outer circle
        CircularProgressIndicator(
            modifier = Modifier.size(70.dp),
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
            strokeWidth = 2.5.dp,
            trackColor = MaterialTheme.colorScheme.surface
        )

        // Inner rotating circle
        CircularProgressIndicator(
            modifier = Modifier
                .size(55.dp)
                .rotate(rotation),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.5.dp,
            strokeCap = StrokeCap.Round
        )

        // Center dot
        Box(
            modifier = Modifier
                .size(15.dp)
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

@Composable
private fun PulsingErrorIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "error_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "error_pulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .wrapContentSize()
            .size(70.dp)
            .scale(scale)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer)
                .border(2.dp, MaterialTheme.colorScheme.error, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.info_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun SuccessCard(onDismiss: () -> Unit) {
    var showCheckmark by remember { mutableStateOf(false) }
    val checkmarkScale by animateFloatAsState(
        targetValue = if (showCheckmark) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "checkmark_scale"
    )

    LaunchedEffect(Unit) {
        delay(200)
        showCheckmark = true
    }

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(0.85f)
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.check_circle_24px),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(40.dp)
                                .scale(checkmarkScale)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(id = R.string.bug_report_success_title), // Use string resource
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(id = R.string.bug_report_success_message), // Use string resource
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.bug_report_action_close), // Use string resource
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SubmissionStatusOverlayPreview() {
    SubmissionStatusOverlay(
        isSubmitting = true,
        isSuccess = false,
        submissionError = null
    )
}

@Preview(showBackground = true)
@Composable
private fun SubmissionStatusOverlayErrorPreview() {
    SubmissionStatusOverlay(
        isSubmitting = true,
        isSuccess = false,
        submissionError = "Ошибка соединения с сервером. Проверьте подключение к интернету."
    )
}

@Preview(showBackground = true)
@Composable
private fun SubmissionStatusOverlaySuccessPreview() {
    SubmissionStatusOverlay(
        isSubmitting = true,
        isSuccess = true,
        submissionError = null
    )
}
