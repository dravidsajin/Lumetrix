package com.lumetrix.statsmanager.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lumetrix.statsmanager.ui.theme.AccentGradientEnd
import com.lumetrix.statsmanager.ui.theme.AccentGradientStart
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.OrbGlow
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary

@Composable
fun FocusScoreOrb(
    score: Int,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
) {
    val transition = rememberInfiniteTransition(label = "orb")
    val breathe by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathe",
    )
    val ringRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
        ),
        label = "ring",
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size * breathe)) {
            val radius = this.size.minDimension / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrbGlow, Color.Transparent),
                    radius = radius,
                ),
                radius = radius,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AccentGradientStart.copy(alpha = 0.35f),
                        AccentGradientEnd.copy(alpha = 0.12f),
                        Color.Transparent,
                    ),
                    radius = radius * 0.85f,
                ),
                radius = radius * 0.85f,
            )
            drawCircle(
                color = AccentPrimary.copy(alpha = 0.25f),
                radius = radius * 0.72f,
                style = Stroke(width = 2.dp.toPx()),
            )
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(AccentGradientStart, AccentGradientEnd, AccentGradientStart),
                ),
                startAngle = ringRotation,
                sweepAngle = 280f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimary,
            )
        }
    }
}
