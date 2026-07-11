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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDetails by remember { mutableStateOf(false) }

    if (showDetails) {
        ReportDetailsSubScreen(onBack = { showDetails = false })
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
                Text("6 – 12 May 2024", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            IconButton(onClick = { Toast.makeText(context, "Share report", Toast.LENGTH_SHORT).show() }) { Icon(Icons.Outlined.Share, "Share", tint = TextPrimary) }
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
                        // Score arc (83/100 => 299 degrees)
                        drawArc(
                            brush = Brush.sweepGradient(listOf(Success, AccentPrimary, Success)),
                            startAngle = -90f,
                            sweepAngle = 83f * 3.6f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("83", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Text("Digital Score", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }

                Text("Great week, David! 🎉", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Success)
            }
        }

        // Top Achievements card
        Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Top Achievements", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)

                val achievements = listOf(
                    Triple("🎯", "4 hours focus streak", AccentSecondary),
                    Triple("📵", "No phone after 10 PM × 5 days", AccentPrimary),
                    Triple("😴", "Average sleep 7h 48m", Color(0xFF26C6DA)),
                    Triple("📈", "Productivity rating: high", Success)
                )
                achievements.forEach { (emoji, text, color) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.06f)).padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                            Text(emoji, style = MaterialTheme.typography.bodyLarge)
                        }
                        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
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
private fun ReportDetailsSubScreen(onBack: () -> Unit) {
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
            "Summary" -> SummaryTab()
            "Insights" -> InsightsTab()
            "Highlights" -> HighlightsTab()
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
private fun SummaryTab() {
    val items = listOf(
        Triple("📱", "Screen Time", "5h 12m avg (↓ 8%)"),
        Triple("🎯", "Focus Time", "2h 34m avg (↑ 15%)"),
        Triple("😴", "Sleep", "7h 48m avg (↑ 3%)"),
        Triple("🔓", "Unlocks", "42 avg (↓ 12%)"),
        Triple("📬", "Notifications", "187 avg (↓ 5%)")
    )
    Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Weekly Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            items.forEach { (emoji, label, value) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(emoji, style = MaterialTheme.typography.bodyLarge)
                        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                    Text(value, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun InsightsTab() {
    val insights = listOf(
        Triple("📸", "Instagram usage increased by 12%", Color(0xFFE040FB)),
        Triple("⏰", "Best focus hours: 9 AM – 12 PM", AccentPrimary),
        Triple("😴", "Sleep consistency improved by 8%", Success),
        Triple("📵", "Phone-free evenings × 5 days", AccentSecondary),
        Triple("📊", "Productivity was high this week", Color(0xFF00E5FF))
    )
    Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("AI Insights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            insights.forEach { (emoji, text, color) ->
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.06f)).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Text(emoji, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HighlightsTab() {
    val highlights = listOf(
        "🏆" to "Longest focus streak: 4h 12m on Wednesday",
        "📖" to "Completed 'Read for 30 mins' goal × 6 days",
        "🚶" to "Best step count: 8,234 steps on Friday",
        "💧" to "Hit water goal 5/7 days",
        "🌙" to "Best sleep: 8h 45m on Saturday"
    )
    Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Week Highlights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            highlights.forEach { (emoji, text) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, style = MaterialTheme.typography.titleMedium)
                    Text(text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
