package com.lumetrix.statsmanager.ui.sleep

import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.GlassCardBorder
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary

@Composable
fun SleepScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    sleepViewModel: SleepViewModel = viewModel()
) {
    val context = LocalContext.current
    var showInsights by remember { mutableStateOf(false) }

    val state by sleepViewModel.sleepState.collectAsState()

    if (showInsights) {
        SleepInsightsSubScreen(
            score = state.sleepScore,
            onBack = { showInsights = false }
        )
        return
    }

    val quality = when {
        state.sleepScore >= 85 -> "Great"
        state.sleepScore >= 70 -> "Good"
        else -> "Fair"
    }
    val qualityColor = when (quality) {
        "Great" -> AccentSecondary
        "Good" -> Success
        else -> Color(0xFFFFB84D)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LumetrixTokens.ScreenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary) }
            Text("Sleep", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            IconButton(onClick = { Toast.makeText(context, "More options", Toast.LENGTH_SHORT).show() }) { Icon(Icons.Outlined.MoreVert, "More", tint = TextPrimary) }
        }

        // Day / Week / Month / Year tabs
        val tabs = listOf("Day", "Week", "Month", "Year")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tabs.forEach { tab ->
                val isSelected = state.selectedFilter == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) AccentSecondary.copy(alpha = 0.2f) else Color.Transparent)
                        .border(1.dp, if (isSelected) AccentSecondary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(50))
                        .clickable { sleepViewModel.setFilter(tab) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(tab, style = MaterialTheme.typography.labelMedium, color = if (isSelected) AccentSecondary else TextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Duration + quality summary
        Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(state.durationLabel, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Text("Avg Sleep Duration", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(quality, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = qualityColor)
                        Text(state.trendLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Success)
                    }
                }

                // Bar chart
                val barData = state.chartPoints.map { it.value }
                if (barData.isNotEmpty()) {
                    SleepBarChart(data = barData, modifier = Modifier.fillMaxWidth().height(120.dp))
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Text("No sleep tracking records for this period", color = TextSecondary)
                    }
                }

                // Day labels
                if (state.selectedFilter == "Week") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                            Text(it, style = MaterialTheme.typography.labelSmall, color = TextSecondary.copy(alpha = 0.7f), modifier = Modifier.width(32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
        }

        // Sleep Stages card
        Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Sleep Stages (Avg)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)

                state.stages.forEach { stage ->
                    val color = try { Color(android.graphics.Color.parseColor(stage.colorHex)) } catch (e: Exception) { AccentPrimary }
                    SleepStageRow(name = stage.name, duration = stage.durationLabel, color = color, fraction = stage.fraction)
                }
            }
        }

        // View Sleep Insights button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(AccentPrimary.copy(alpha = 0.1f), AccentSecondary.copy(alpha = 0.1f))))
                .border(1.dp, AccentPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .clickable { showInsights = true }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("View Sleep Insights", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = AccentPrimary)
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun SleepBarChart(data: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val barCount = data.size
        val maxVal = 10f
        val barSpacing = 6.dp.toPx()
        val barWidth = (size.width - (barCount - 1) * barSpacing) / barCount

        // Grid lines
        for (i in 0..2) {
            val y = size.height * i / 2f
            drawLine(Color.White.copy(alpha = 0.04f), Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
        }

        data.forEachIndexed { idx, value ->
            val barHeight = ((value / maxVal) * size.height).coerceAtLeast(10f)
            val x = idx * (barWidth + barSpacing)
            val topLeft = Offset(x, size.height - barHeight)

            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Color(0xFF8126F2), Color(0xFF5E35B1)), startY = topLeft.y, endY = size.height),
                topLeft = topLeft,
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

@Composable
private fun SleepStageRow(name: String, duration: String, color: Color, fraction: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                Text(name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
            }
            Text(duration, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f))) {
            Box(modifier = Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(4.dp).clip(CircleShape).background(color))
        }
    }
}

// ── Sleep Insights Sub-Screen ──

@Composable
private fun SleepInsightsSubScreen(score: Int, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LumetrixTokens.ScreenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary) }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sleep Insights", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        // Insights header
        Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🌙", style = MaterialTheme.typography.headlineMedium)
                Text("Sleeping well helps you stay productive and happy", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("Based on your sleep data from the past week", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        // Recommendations
        Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Recommendations", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)

                val recs = listOf(
                    "💤" to "Try sleeping 30 mins earlier",
                    "📵" to "Avoid screens 1 hour before bed",
                    "🌡️" to "Keep your room dark and cool",
                    "☕" to "No caffeine after 2 PM"
                )
                recs.forEach { (icon, text) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(AccentSecondary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Text(icon, style = MaterialTheme.typography.bodyLarge)
                        }
                        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                }
            }
        }

        // Quality summary
        Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sleep Quality", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Your sleep quality is good this week", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(60.dp)) {
                            drawArc(Color.White.copy(alpha = 0.05f), 0f, 360f, false, style = Stroke(6.dp.toPx(), cap = StrokeCap.Round))
                            drawArc(brush = Brush.sweepGradient(listOf(Success, AccentPrimary, Success)), startAngle = -90f, sweepAngle = score * 3.6f, useCenter = false, style = Stroke(6.dp.toPx(), cap = StrokeCap.Round))
                        }
                        Text(score.toString(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Success)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
