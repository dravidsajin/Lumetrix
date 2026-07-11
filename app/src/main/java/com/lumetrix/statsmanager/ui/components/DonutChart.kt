package com.lumetrix.statsmanager.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary

data class DonutSegment(
    val label: String,
    val value: Float,
    val color: Color,
)

@Composable
fun DonutChart(
    segments: List<DonutSegment>,
    totalScreenTimeMs: Long,
    modifier: Modifier = Modifier,
) {
    var activeCategory by remember { mutableStateOf("Productive") }
    val progress = remember { Animatable(0f) }
    val total = segments.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)

    LaunchedEffect(segments) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(1200))
    }

    // Infinite breathing neon pulse animation for the selected active ring
    val infiniteTransition = rememberInfiniteTransition(label = "ringPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val pulseWidthOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseWidth"
    )

    // Segment values
    val productive = segments.firstOrNull { it.label == "Productive" }
    val neutral = segments.firstOrNull { it.label == "Neutral" }
    val distracting = segments.firstOrNull { it.label == "Distracting" }

    val productivePct = ((productive?.value ?: 0f) / total) * progress.value
    val neutralPct = ((neutral?.value ?: 0f) / total) * progress.value
    val distractingPct = ((distracting?.value ?: 0f) / total) * progress.value

    // Dynamic stats centered in concentric rings
    val activeSegment = when (activeCategory) {
        "Productive" -> productive
        "Neutral" -> neutral
        "Distracting" -> distracting
        else -> productive
    }
    val activePctValue = when (activeCategory) {
        "Productive" -> (productive?.value ?: 0f) / total
        "Neutral" -> (neutral?.value ?: 0f) / total
        "Distracting" -> (distracting?.value ?: 0f) / total
        else -> 0f
    }
    val categoryDurationMs = (activePctValue * totalScreenTimeMs).toLong()
    val durationLabel = formatMsToHoursMinutes(categoryDurationMs)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Glowing Concentric Rings Chart Box
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(140.dp)) {
                val strokeWidth = 9.dp.toPx()

                // 1. PRODUCTIVE RING (Outer, Radius: 56.dp)
                val radiusOuter = 56.dp.toPx()
                drawCircle(
                    color = (productive?.color ?: Color.Green).copy(alpha = 0.05f),
                    radius = radiusOuter,
                    style = Stroke(width = strokeWidth)
                )
                if (productivePct > 0f) {
                    val angleSweep = productivePct * 360f
                    // If selected, draw glowing breathing aura
                    if (activeCategory == "Productive") {
                        drawArc(
                            color = (productive?.color ?: Color.Green).copy(alpha = pulseAlpha * 0.15f),
                            startAngle = -90f,
                            sweepAngle = angleSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth + pulseWidthOffset, cap = StrokeCap.Round)
                        )
                    }
                    // Main Arc
                    drawArc(
                        color = productive?.color ?: Color.Green,
                        startAngle = -90f,
                        sweepAngle = angleSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // 2. NEUTRAL RING (Middle, Radius: 42.dp)
                val radiusMiddle = 42.dp.toPx()
                drawCircle(
                    color = (neutral?.color ?: Color.Yellow).copy(alpha = 0.05f),
                    radius = radiusMiddle,
                    style = Stroke(width = strokeWidth)
                )
                if (neutralPct > 0f) {
                    val angleSweep = neutralPct * 360f
                    if (activeCategory == "Neutral") {
                        drawArc(
                            color = (neutral?.color ?: Color.Yellow).copy(alpha = pulseAlpha * 0.15f),
                            startAngle = -90f,
                            sweepAngle = angleSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth + pulseWidthOffset, cap = StrokeCap.Round)
                        )
                    }
                    drawArc(
                        color = neutral?.color ?: Color.Yellow,
                        startAngle = -90f,
                        sweepAngle = angleSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // 3. DISTRACTING RING (Inner, Radius: 28.dp)
                val radiusInner = 28.dp.toPx()
                drawCircle(
                    color = (distracting?.color ?: Color.Red).copy(alpha = 0.05f),
                    radius = radiusInner,
                    style = Stroke(width = strokeWidth)
                )
                if (distractingPct > 0f) {
                    val angleSweep = distractingPct * 360f
                    if (activeCategory == "Distracting") {
                        drawArc(
                            color = (distracting?.color ?: Color.Red).copy(alpha = pulseAlpha * 0.15f),
                            startAngle = -90f,
                            sweepAngle = angleSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth + pulseWidthOffset, cap = StrokeCap.Round)
                        )
                    }
                    drawArc(
                        color = distracting?.color ?: Color.Red,
                        startAngle = -90f,
                        sweepAngle = angleSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
            
            // Text center metrics
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = durationLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = activeCategory,
                    style = MaterialTheme.typography.labelSmall,
                    color = activeSegment?.color ?: TextSecondary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Detailed Cards Legend Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            segments.forEach { segment ->
                val isSelected = activeCategory == segment.label
                val segmentPct = ((segment.value / total) * 100).toInt()
                val segmentDurationMs = ((segment.value / total) * totalScreenTimeMs).toLong()
                val segmentDurationLabel = formatMsToHoursMinutes(segmentDurationMs)

                val borderStroke = if (isSelected) {
                    BorderStroke(1.5.dp, segment.color)
                } else {
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                }

                val backgroundAlpha = if (isSelected) 0.08f else 0.03f

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { activeCategory = segment.label },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    border = borderStroke
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(segment.color.copy(alpha = backgroundAlpha), Color.Transparent)
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(segment.color)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = segment.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "$segmentDurationLabel / day",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "$segmentPct%",
                                style = MaterialTheme.typography.titleMedium,
                                color = segment.color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatMsToHoursMinutes(ms: Long): String {
    if (ms <= 0L) return "0m"
    val totalSeconds = ms / 1000
    val totalMinutes = totalSeconds / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
