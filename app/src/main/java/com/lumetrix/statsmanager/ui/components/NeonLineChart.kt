package com.lumetrix.statsmanager.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumetrix.statsmanager.domain.model.ChartDataPoint
import com.lumetrix.statsmanager.ui.theme.AccentGradientEnd
import com.lumetrix.statsmanager.ui.theme.AccentGradientStart
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.Divider
import com.lumetrix.statsmanager.ui.theme.TextSecondary

@Composable
fun NeonLineChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    val haptic = LocalHapticFeedback.current
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(data) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(1400))
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 16.dp)
            .pointerInput(data) {
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = { hoveredIndex = null },
                    onDragCancel = { hoveredIndex = null },
                    onDrag = { change, _ ->
                        val x = change.position.x
                        if (data.size > 1) {
                            val leftPadding = 30.dp.toPx()
                            val chartWidth = size.width - leftPadding
                            val stepX = chartWidth / (data.lastIndex)
                            val index = ((x - leftPadding + (stepX / 2)) / stepX).toInt().coerceIn(0, data.lastIndex)
                            if (hoveredIndex != index) {
                                hoveredIndex = index
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    }
                )
            }
            .pointerInput(data) {
                detectTapGestures(
                    onTap = { offset: Offset ->
                        val x = offset.x
                        if (data.size > 1) {
                            val leftPadding = 30.dp.toPx()
                            val chartWidth = size.width - leftPadding
                            val stepX = chartWidth / (data.lastIndex)
                            val index = ((x - leftPadding + (stepX / 2)) / stepX).toInt().coerceIn(0, data.lastIndex)
                            if (hoveredIndex == index) {
                                hoveredIndex = null
                            } else {
                                hoveredIndex = index
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    }
                )
            },
    ) {
        if (data.size < 2) return@Canvas

        val maxVal = data.maxOf { it.value }.coerceAtLeast(1f)
        val graphMax = (maxVal * 1.2f)
        
        val leftPadding = 30.dp.toPx()   // wider so Y labels don't collide with the line
        val bottomPadding = 24.dp.toPx()
        
        val chartWidth = size.width - leftPadding
        val chartHeight = size.height - bottomPadding

        val stepX = chartWidth / (data.lastIndex)
        val points = data.mapIndexed { index, point ->
            Offset(
                x = leftPadding + (index * stepX),
                y = chartHeight - (point.value / graphMax) * chartHeight,
            )
        }

        val visibleCount = (points.size * progress.value).toInt().coerceAtLeast(2)
        val visiblePoints = points.take(visibleCount)

        val gridLines = 4
        val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        for (i in 0..gridLines) {
            val y = chartHeight - (chartHeight * (i / gridLines.toFloat()))
            val labelValue = graphMax * (i / gridLines.toFloat())
            
            drawLine(
                color = Divider.copy(alpha = 0.5f),
                start = Offset(leftPadding, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
                pathEffect = dashedEffect
            )
            
            if (i > 0) {
                val labelText = String.format("%.0fh", labelValue)
                val labelLayout = textMeasurer.measure(
                    text = labelText,
                    style = TextStyle(color = TextSecondary, fontSize = 10.sp)
                )
                drawText(
                    textLayoutResult = labelLayout,
                    topLeft = Offset(
                        x = leftPadding - labelLayout.size.width - 8.dp.toPx(),  // 8dp gap from line
                        y = y - (labelLayout.size.height / 2)
                    )
                )
            }
        }

        val averageVal = data.map { it.value }.average().toFloat()
        val averageY = chartHeight - (averageVal / graphMax) * chartHeight
        val avgDashEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
        drawLine(
            color = AccentSecondary.copy(alpha = 0.5f),
            start = Offset(leftPadding, averageY),
            end = Offset(size.width, averageY),
            strokeWidth = 2f,
            pathEffect = avgDashEffect
        )

        data.forEachIndexed { index, point ->
            val labelLayout = textMeasurer.measure(
                text = point.dayLabel,
                style = TextStyle(color = TextSecondary, fontSize = 10.sp)
            )
            drawText(
                textLayoutResult = labelLayout,
                topLeft = Offset(
                    x = leftPadding + (index * stepX) - (labelLayout.size.width / 2),
                    y = size.height - labelLayout.size.height
                )
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

        val fillPath = Path().apply {
            addPath(path)
            lineTo(visiblePoints.last().x, chartHeight)
            lineTo(visiblePoints.first().x, chartHeight)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    AccentGradientStart.copy(alpha = 0.4f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = chartHeight
            ),
            style = Fill
        )

        drawPath(
            path = path,
            brush = Brush.linearGradient(listOf(AccentGradientStart, AccentGradientEnd)),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
        )

        visiblePoints.forEachIndexed { index, point ->
            val isHovered = hoveredIndex == index
            drawCircle(
                color = if (isHovered) Color.White else AccentPrimary,
                radius = if (isHovered) 7.dp.toPx() else 5.dp.toPx(),
                center = point,
            )
            drawCircle(
                color = AccentPrimary.copy(alpha = if (isHovered) 0.5f else 0.25f),
                radius = if (isHovered) 14.dp.toPx() else 10.dp.toPx(),
                center = point,
            )

            if (isHovered) {
                drawLine(
                    color = AccentPrimary.copy(alpha = 0.3f),
                    start = Offset(point.x, point.y),
                    end = Offset(point.x, chartHeight),
                    strokeWidth = 2.dp.toPx()
                )

                val valueStr = data[index].formattedLabel.ifEmpty { String.format("%.1f h", data[index].value) }
                val measuredText = textMeasurer.measure(
                    text = valueStr,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                val tooltipWidth = measuredText.size.width + 16.dp.toPx()
                val tooltipHeight = measuredText.size.height + 8.dp.toPx()
                
                val tooltipX = (point.x - tooltipWidth / 2).coerceIn(leftPadding, size.width - tooltipWidth)
                val tooltipY = (point.y - tooltipHeight - 12.dp.toPx()).coerceAtLeast(0f)

                drawRoundRect(
                    color = AccentGradientStart,
                    topLeft = Offset(tooltipX, tooltipY),
                    size = Size(tooltipWidth, tooltipHeight),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
                
                drawText(
                    textLayoutResult = measuredText,
                    topLeft = Offset(
                        tooltipX + (tooltipWidth - measuredText.size.width) / 2,
                        tooltipY + (tooltipHeight - measuredText.size.height) / 2
                    )
                )
            }
        }
    }
}
