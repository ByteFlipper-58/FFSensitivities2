package com.byteflipper.ffsensitivities.presentation.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ShapeDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Cached shimmer colors for better performance
private val shimmerColors = listOf(
    Color.LightGray.copy(alpha = 0.6f),
    Color.LightGray.copy(alpha = 0.2f),
    Color.LightGray.copy(alpha = 0.6f)
)

@Composable
fun AnimatedShimmer() {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500, // Reduced duration for better performance
                easing = FastOutSlowInEasing
            )
        ),
        label = "shimmer_animation"
    )

    val brush = remember(translateAnimation.value) {
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    }

    Column {
        ShimmerLazyItem(brush = brush)
        ShimmerGridItem(brush = brush)
    }
}

@Composable
fun ShimmerGridItem(brush: Brush) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(brush)
        )

        Spacer(modifier = Modifier.width(10.dp))
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(
                modifier = Modifier
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .fillMaxWidth(fraction = 0.7f)
                    .background(brush)
            )

            Spacer(modifier = Modifier.padding(5.dp))

            Spacer(
                modifier = Modifier
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .fillMaxWidth(fraction = 0.9f)
                    .background(brush)
            )

            Spacer(modifier = Modifier.padding(5.dp))

            Spacer(
                modifier = Modifier
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .fillMaxWidth(fraction = 0.35f)
                    .background(brush)
            )
        }
    }
}

@Composable
fun ShimmerLazyItem(brush: Brush? = null) {
    val transition = rememberInfiniteTransition(label = "lazy_shimmer_transition")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f, // Reduced target value
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200, // Slightly faster
                easing = FastOutSlowInEasing
            )
        ),
        label = "lazy_shimmer_animation"
    )

    val shimmerBrush = brush ?: remember(translateAnimation.value) {
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = ShapeDefaults.Large,
        onClick = {
            // Empty click handler for shimmer
        }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(
                modifier = Modifier
                    .padding(4.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .fillMaxWidth()
                    .background(shimmerBrush)
            )
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AnimatedShimmerPreview() {
    AnimatedShimmer()
}
