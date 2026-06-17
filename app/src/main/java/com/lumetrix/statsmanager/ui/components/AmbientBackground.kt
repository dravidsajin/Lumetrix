package com.lumetrix.statsmanager.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.lumetrix.statsmanager.ui.theme.AmbientCyan
import com.lumetrix.statsmanager.ui.theme.AmbientPurple
import com.lumetrix.statsmanager.ui.theme.BackgroundPrimary
import com.lumetrix.statsmanager.ui.theme.BackgroundSecondary

@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "ambient")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "drift",
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundPrimary, BackgroundSecondary, BackgroundPrimary),
                ),
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AmbientPurple, Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * (0.15f + drift * 0.05f)),
                    radius = size.width * 0.55f,
                ),
                radius = size.width * 0.55f,
                center = Offset(size.width * 0.2f, size.height * (0.15f + drift * 0.05f)),
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AmbientCyan, Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * (0.75f - drift * 0.04f)),
                    radius = size.width * 0.45f,
                ),
                radius = size.width * 0.45f,
                center = Offset(size.width * 0.85f, size.height * (0.75f - drift * 0.04f)),
            )
        }
    }
}
