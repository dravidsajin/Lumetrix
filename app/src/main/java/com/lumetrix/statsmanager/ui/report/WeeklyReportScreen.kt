package com.lumetrix.statsmanager.ui.report

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
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun WeeklyReportScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    weeklyReportViewModel: WeeklyReportViewModel = viewModel()
) {
    val context = LocalContext.current
    var showDetails by remember { mutableStateOf(false) }
    val state by weeklyReportViewModel.reportState.collectAsState()

    if (showDetails) {
        ReportDetailsSubScreen(
            state = state,
            onBack = { showDetails = false }
        )
        return
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Weekly Report", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(state.dateRange, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            IconButton(onClick = { Toast.makeText(context, "Share report details", Toast.LENGTH_SHORT).show() }) { Icon(Icons.Outlined.Share, "Share", tint = TextPrimary) }
        }

        // Score gauge card
        Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Circular score
                Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(160.dp)) {
                        val strokeWidth = 10.dp.toPx()
                        // Track
                        drawArc(Color.White.copy(alpha = 0.05f), 0f, 360f, false, style = Stroke(strokeWidth, cap = StrokeCap.Round))
                        // Score arc
                        drawArc(
                            brush = Brush.sweepGradient(listOf(Success, AccentPrimary, Success)),
                            startAngle = -90f,
                            sweepAngle = state.digitalScore * 3.6f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.digitalScore.toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Text("Digital Score", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }

                Text(state.greeting, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Success)
            }
        }

        // Top Achievements card
        Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Top Achievements", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)

                state.achievements.forEach { achievement ->
                    val color = try { Color(android.graphics.Color.parseColor(achievement.colorHex)) } catch (e: Exception) { AccentPrimary }
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.06f)).padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                            Text(achievement.emoji, style = MaterialTheme.typography.bodyLarge)
                        }
                        Text(achievement.text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                    }
                }
            }
        }

        // View Report Details button
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(AccentPrimary.copy(alpha = 0.1f), AccentSecondary.copy(alpha = 0.1f))))
                .border(1.dp, AccentPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .clickable { showDetails = true }.padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("View Report Details", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = AccentPrimary)
        }

        // Share Report button
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(Success, Color(0xFF00C853))))
                .clickable { Toast.makeText(context, "Sharing report...", Toast.LENGTH_SHORT).show() }.padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Share Report", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

// ── Report Details Sub-Screen ──

@Composable
private fun ReportDetailsSubScreen(
    state: WeeklyReportUiState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("Summary") }

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
            Text("Report Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        // Summary / Insights / Highlights tabs
        val tabs = listOf("Summary", "Insights", "Highlights")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(50))
                        .background(if (isSelected) AccentSecondary.copy(alpha = 0.2f) else Color.Transparent)
                        .border(1.dp, if (isSelected) AccentSecondary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(50))
                        .clickable { selectedTab = tab }.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(tab, style = MaterialTheme.typography.labelMedium, color = if (isSelected) AccentSecondary else TextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        }

        when (selectedTab) {
            "Summary" -> SummaryTab(state.summaryItems)
            "Insights" -> InsightsTab(state.insights)
            "Highlights" -> HighlightsTab(state.highlights)
        }

        // Share Report button
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(Success, Color(0xFF00C853))))
                .clickable { Toast.makeText(context, "Sharing report...", Toast.LENGTH_SHORT).show() }.padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Share Report", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun SummaryTab(items: List<ReportSummaryItem>) {
    Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Weekly Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(item.emoji, style = MaterialTheme.typography.bodyLarge)
                        Text(item.label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                    Text(item.valueLabel, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun InsightsTab(insights: List<ReportInsightItem>) {
    Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("AI Insights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            insights.forEach { insight ->
                val color = try { Color(android.graphics.Color.parseColor(insight.colorHex)) } catch (e: Exception) { AccentPrimary }
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.06f)).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Text(insight.emoji, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(insight.text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HighlightsTab(highlights: List<String>) {
    val emojis = listOf("🏆", "📖", "🚶", "💧", "🌙")
    Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Week Highlights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            highlights.forEachIndexed { idx, highlight ->
                val emoji = emojis.getOrElse(idx) { "🌟" }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, style = MaterialTheme.typography.titleMedium)
                    Text(highlight, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
