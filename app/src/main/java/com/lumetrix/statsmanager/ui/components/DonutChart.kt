package com.lumetrix.statsmanager.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.lumetrix.statsmanager.ui.theme.TextSecondary

data class DonutSegment(
    val label: String,
    val value: Float,
    val color: Color,
)

@Composable
fun DonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    val total = segments.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)

    LaunchedEffect(segments) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(1200))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(120.dp)) {
                var startAngle = -90f
                segments.forEach { segment ->
                    val sweep = (segment.value / total) * 360f * progress.value
                    drawArc(
                        color = segment.color.copy(alpha = 0.85f),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round),
                    )
                    startAngle += sweep
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            segments.forEach { segment ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawCircle(color = segment.color)
                    }
                    Text(
                        text = segment.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${(segment.value / total * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = segment.color,
                    )
                }
            }
        }
    }
}
