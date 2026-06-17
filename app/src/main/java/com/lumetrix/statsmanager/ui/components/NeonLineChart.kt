package com.lumetrix.statsmanager.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.lumetrix.statsmanager.ui.theme.AccentGradientEnd
import com.lumetrix.statsmanager.ui.theme.AccentGradientStart
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.Divider
import com.lumetrix.statsmanager.ui.theme.TextSecondary

@Composable
fun NeonLineChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(1400))
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
    ) {
        if (data.size < 2) return@Canvas

        val max = data.max().coerceAtLeast(1f)
        val stepX = size.width / (data.lastIndex)
        val points = data.mapIndexed { index, value ->
            Offset(
                x = index * stepX,
                y = size.height - (value / max) * size.height * 0.85f,
            )
        }

        val visibleCount = (points.size * progress.value).toInt().coerceAtLeast(2)
        val visiblePoints = points.take(visibleCount)

        for (index in 0 until 4) {
            val y = size.height * (index / 3f)
            drawLine(
                color = Divider,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
            )
        }

        val path = Path().apply {
            moveTo(visiblePoints.first().x, visiblePoints.first().y)
            for (index in 1 until visiblePoints.size) {
                val previous = visiblePoints[index - 1]
                val current = visiblePoints[index]
                val controlX = (previous.x + current.x) / 2f
                cubicTo(controlX, previous.y, controlX, current.y, current.x, current.y)
            }
        }

        drawPath(
            path = path,
            brush = Brush.linearGradient(listOf(AccentGradientStart, AccentGradientEnd)),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
        )

        visiblePoints.forEach { point ->
            drawCircle(
                color = AccentPrimary,
                radius = 5.dp.toPx(),
                center = point,
            )
            drawCircle(
                color = AccentPrimary.copy(alpha = 0.25f),
                radius = 10.dp.toPx(),
                center = point,
            )
        }

        drawLine(
            color = TextSecondary.copy(alpha = 0.2f),
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1f,
        )
    }
}
