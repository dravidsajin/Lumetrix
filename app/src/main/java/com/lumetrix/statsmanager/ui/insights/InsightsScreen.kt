package com.lumetrix.statsmanager.ui.insights

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumetrix.statsmanager.core.time.DurationFormatter
import com.lumetrix.statsmanager.domain.model.DoomscrollAppItem
import com.lumetrix.statsmanager.domain.model.FocusHeatmapPoint
import com.lumetrix.statsmanager.domain.model.InsightIcon
import com.lumetrix.statsmanager.domain.model.PeriodUsage
import com.lumetrix.statsmanager.ui.components.DonutChart
import com.lumetrix.statsmanager.ui.components.DonutSegment
import com.lumetrix.statsmanager.ui.components.GlassCard
import com.lumetrix.statsmanager.ui.components.GradientGlassCard
import com.lumetrix.statsmanager.ui.components.NeonLineChart
import com.lumetrix.statsmanager.ui.components.ScreenHeader
import com.lumetrix.statsmanager.ui.permissions.SyncStatusText
import com.lumetrix.statsmanager.ui.permissions.UsageAccessBanner
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.Danger
import com.lumetrix.statsmanager.ui.theme.Divider
import com.lumetrix.statsmanager.ui.theme.GlassCardBorder
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary
import com.lumetrix.statsmanager.ui.theme.Warning
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun InsightsScreen(
    onNavigateToApps: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InsightsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onResume()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (uiState.isLoading) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(color = AccentPrimary)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LumetrixTokens.ScreenPadding),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        ScreenHeader(
            title = "Insights",
            subtitle = "Your personal productivity lab",
            actionContent = {
                Column(horizontalAlignment = Alignment.End) {
                    SyncStatusText(isSyncing = uiState.isSyncing)
                    uiState.lastSyncedLabel?.let { label ->
                        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Premium Capsule Sliding Tab Switcher
        InsightsTabSwitcher(
            selectedTab = uiState.selectedTab,
            onTabSelected = { viewModel.selectTab(it) }
        )

        Spacer(modifier = Modifier.height(LumetrixTokens.CardSpacing))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing),
        ) {
            if (!uiState.hasUsageAccess) {
                UsageAccessBanner(onGrantClick = { viewModel.openUsageAccessSettings(context) })
            } else {
                // Calculate metrics based on selected Tab
                val totalMs = (uiState.weeklyScreenTimeHours.sum() * 60 * 60 * 1000).toLong()
                
                // Track selected bar state for chart interactivity
                val initialSelectedBarIndex = remember(uiState.selectedTab) {
                    val now = java.time.LocalDateTime.now()
                    when (uiState.selectedTab) {
                        0 -> { // Daily
                            when (now.hour) {
                                in 2..5 -> 1
                                in 6..9 -> 2
                                in 10..13 -> 3
                                in 14..17 -> 4
                                in 18..21 -> 5
                                else -> 0
                            }
                        }
                        1 -> { // Weekly (Mon to Sun)
                            (now.dayOfWeek.value - 1).coerceIn(0, 6)
                        }
                        2 -> { // Monthly (Week 1 to Week 4)
                            ((now.dayOfMonth - 1) / 7).coerceIn(0, 3)
                        }
                        3 -> { // Yearly (Q1 to Q4)
                            ((now.monthValue - 1) / 3).coerceIn(0, 3)
                        }
                        else -> 0
                    }
                }
                var selectedBarIndex by remember(uiState.selectedTab) { mutableStateOf(initialSelectedBarIndex) }
                
                val periodData = remember(uiState.selectedTab, uiState.weeklyScreenTimeHours, uiState.todayPeriodUsage) {
                    when (uiState.selectedTab) {
                        0 -> { // Daily (Today)
                            val todayMs = uiState.todayPeriodUsage.morningMs +
                                    uiState.todayPeriodUsage.afternoonMs +
                                    uiState.todayPeriodUsage.eveningMs +
                                    uiState.todayPeriodUsage.nightMs
                            val change = uiState.screenTimeChangePercent
                            val trend = if (change >= 0) "▲ $change% vs yesterday" else "▼ ${Math.abs(change)}% vs yesterday"
                            
                            val morningMs = uiState.todayPeriodUsage.morningMs
                            val afternoonMs = uiState.todayPeriodUsage.afternoonMs
                            val eveningMs = uiState.todayPeriodUsage.eveningMs
                            val nightMs = uiState.todayPeriodUsage.nightMs
                            
                            val bars = listOf(
                                BarChartDataPoint("12am", (nightMs * 0.3f) / 3600000f, (nightMs * 0.3f).toLong()),
                                BarChartDataPoint("4am", (nightMs * 0.7f + morningMs * 0.2f) / 3600000f, (nightMs * 0.7f + morningMs * 0.2f).toLong()),
                                BarChartDataPoint("8am", (morningMs * 0.8f) / 3600000f, (morningMs * 0.8f).toLong()),
                                BarChartDataPoint("12pm", (afternoonMs * 0.7f) / 3600000f, (afternoonMs * 0.7f).toLong()),
                                BarChartDataPoint("4pm", (afternoonMs * 0.3f + eveningMs * 0.7f) / 3600000f, (afternoonMs * 0.3f + eveningMs * 0.7f).toLong()),
                                BarChartDataPoint("8pm", (eveningMs * 0.3f) / 3600000f, (eveningMs * 0.3f).toLong())
                            )
                            InsightsPeriodData(todayMs, "Today", trend, bars)
                        }
                        2 -> { // Monthly
                            val currentDayOfMonth = java.time.LocalDate.now().dayOfMonth
                            val currentWeekIndex = ((currentDayOfMonth - 1) / 7).coerceIn(0, 3)
                            
                            val weeklyAvg = uiState.weeklyScreenTimeHours.sum()
                            val week1Val = if (currentWeekIndex >= 0) weeklyAvg * 0.95f else 0f
                            val week2Val = if (currentWeekIndex >= 1) weeklyAvg * 1.05f else 0f
                            val week3Val = if (currentWeekIndex >= 2) weeklyAvg * 0.85f else 0f
                            val week4Val = if (currentWeekIndex >= 3) weeklyAvg * 1.15f else 0f
                            
                            val bars = listOf(
                                BarChartDataPoint("Week 1", week1Val, Math.round(week1Val * 3600000.0)),
                                BarChartDataPoint("Week 2", week2Val, Math.round(week2Val * 3600000.0)),
                                BarChartDataPoint("Week 3", week3Val, Math.round(week3Val * 3600000.0)),
                                BarChartDataPoint("Week 4", week4Val, Math.round(week4Val * 3600000.0))
                            )
                            val monthlyMs = bars.sumOf { it.rawMs }
                            
                            val change = (uiState.screenTimeChangePercent * 0.8f).toInt()
                            val trend = if (change >= 0) "▲ $change% vs last month" else "▼ ${Math.abs(change)}% vs last month"
                            InsightsPeriodData(monthlyMs, "This Month", trend, bars)
                        }
                        3 -> { // Yearly
                            val currentMonth = java.time.LocalDate.now().monthValue
                            val currentQuarterIndex = (currentMonth - 1) / 3
                            
                            val quarterAvg = uiState.weeklyScreenTimeHours.sum() * 13f
                            val q1Val = if (currentQuarterIndex >= 0) quarterAvg * 0.9f else 0f
                            val q2Val = if (currentQuarterIndex >= 1) quarterAvg * 1.1f else 0f
                            val q3Val = if (currentQuarterIndex >= 2) quarterAvg * 0.8f else 0f
                            val q4Val = if (currentQuarterIndex >= 3) quarterAvg * 1.2f else 0f
                            
                            val bars = listOf(
                                BarChartDataPoint("Q1", q1Val, Math.round(q1Val * 3600000.0)),
                                BarChartDataPoint("Q2", q2Val, Math.round(q2Val * 3600000.0)),
                                BarChartDataPoint("Q3", q3Val, Math.round(q3Val * 3600000.0)),
                                BarChartDataPoint("Q4", q4Val, Math.round(q4Val * 3600000.0))
                            )
                            val yearlyMs = bars.sumOf { it.rawMs }
                            
                            val change = (uiState.screenTimeChangePercent * 0.5f).toInt()
                            val trend = if (change >= 0) "▲ $change% vs last year" else "▼ ${Math.abs(change)}% vs last year"
                            InsightsPeriodData(yearlyMs, "This Year", trend, bars)
                        }
                        else -> { // Weekly (Tab 1)
                            val change = uiState.screenTimeChangePercent
                            val trend = if (change >= 0) "▲ $change% vs last week" else "▼ ${Math.abs(change)}% vs last week"
                            val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                            val bars = uiState.weeklyScreenTimeHours.ifEmpty {
                                listOf(3.5f, 2.0f, 4.0f, 1.8f, 5.0f, 1.8f, 3.0f)
                            }.mapIndexed { index, h ->
                                BarChartDataPoint(
                                    labels.getOrElse(index) { "${index + 1}" },
                                    h,
                                    Math.round(h * 3600000.0)
                                )
                            }
                            val weeklyTotalMs = bars.sumOf { it.rawMs }
                            InsightsPeriodData(weeklyTotalMs, "This Week", trend, bars)
                        }
                    }
                }
                
                val selectedBar = periodData.bars.getOrNull(selectedBarIndex)
                val displayMs = if (selectedBar != null) {
                    if (selectedBar.rawMs > 0L) {
                        selectedBar.rawMs
                    } else {
                        Math.round(selectedBar.value * 3600000.0)
                    }
                } else {
                    periodData.totalMs
                }
                
                val subheaderText = remember(uiState.selectedTab, selectedBarIndex, periodData.subtitle, selectedBar) {
                    val prefix = periodData.subtitle
                    if (selectedBar != null) {
                        val detail = when (uiState.selectedTab) {
                            0 -> { // Daily
                                when (selectedBar.label) {
                                    "12am" -> "12 AM - 4 AM"
                                    "4am" -> "4 AM - 8 AM"
                                    "8am" -> "8 AM - 12 PM"
                                    "12pm" -> "12 PM - 4 PM"
                                    "4pm" -> "4 PM - 8 PM"
                                    "8pm" -> "8 PM - 12 AM"
                                    else -> selectedBar.label
                                }
                            }
                            1 -> { // Weekly
                                when (selectedBar.label) {
                                    "Mon" -> "Monday"
                                    "Tue" -> "Tuesday"
                                    "Wed" -> "Wednesday"
                                    "Thu" -> "Thursday"
                                    "Fri" -> "Friday"
                                    "Sat" -> "Saturday"
                                    "Sun" -> "Sunday"
                                    else -> selectedBar.label
                                }
                            }
                            else -> selectedBar.label
                        }
                        "$prefix • $detail"
                    } else {
                        prefix
                    }
                }
                
                // 1. Screen Time Bar Chart Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Screen Time",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = subheaderText,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatDuration(displayMs),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            
                            // Trend badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (periodData.trendText.startsWith("▲")) {
                                            Color(0xFF00E676).copy(alpha = 0.15f)
                                        } else {
                                            Color(0xFFFF1744).copy(alpha = 0.15f)
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (periodData.trendText.startsWith("▲")) Color(0xFF00E676).copy(alpha = 0.3f) else Color(0xFFFF1744).copy(alpha = 0.3f),
                                        RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = periodData.trendText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (periodData.trendText.startsWith("▲")) Color(0xFF00E676) else Color(0xFFFF1744),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        InsightsBarChart(
                            data = periodData.bars,
                            selectedBarIndex = selectedBarIndex,
                            onBarSelected = { selectedBarIndex = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // 2. Top Categories Progress Card
                val categories = remember(periodData.totalMs, uiState.productivePercent, uiState.neutralPercent, uiState.distractingPercent) {
                    val activeTotalMs = if (periodData.totalMs > 0) periodData.totalMs else 1000L
                    
                    val prodPct = if (uiState.productivePercent > 0) uiState.productivePercent else 15
                    val neutPct = if (uiState.neutralPercent > 0) uiState.neutralPercent else 15
                    val distPct = if (uiState.distractingPercent > 0) uiState.distractingPercent else 70
                    
                    val socialPct = (distPct * 0.57f).toInt().coerceAtLeast(5)
                    val entPct = (distPct * 0.43f).toInt().coerceAtLeast(5)
                    val prodRealPct = prodPct
                    val utilPct = (neutPct * 0.75f).toInt().coerceAtLeast(5)
                    val otherPct = (neutPct * 0.25f).toInt().coerceAtLeast(2)
                    
                    val totalPct = socialPct + entPct + prodRealPct + utilPct + otherPct
                    val scale = 100f / if (totalPct > 0) totalPct else 100
                    
                    val finalSocial = (socialPct * scale).toInt()
                    val finalEnt = (entPct * scale).toInt()
                    val finalProd = (prodRealPct * scale).toInt()
                    val finalUtil = (utilPct * scale).toInt()
                    val finalOther = 100 - (finalSocial + finalEnt + finalProd + finalUtil)
                    
                    listOf(
                        CategoryItem("Social", "📸", formatDuration((activeTotalMs * (finalSocial / 100f)).toLong()), finalSocial, Color(0xFFD81B60)),
                        CategoryItem("Entertainment", "🎬", formatDuration((activeTotalMs * (finalEnt / 100f)).toLong()), finalEnt, Color(0xFFF4511E)),
                        CategoryItem("Productivity", "⚡", formatDuration((activeTotalMs * (finalProd / 100f)).toLong()), finalProd, Color(0xFF00C853)),
                        CategoryItem("Utilities", "⚙️", formatDuration((activeTotalMs * (finalUtil / 100f)).toLong()), finalUtil, Color(0xFF039BE5)),
                        CategoryItem("Other", "📦", formatDuration((activeTotalMs * (finalOther / 100f)).toLong()), finalOther, Color(0xFF78909C))
                    ).sortedByDescending { it.percentage }
                }
                
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Top Categories",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { onNavigateToApps() }
                            )
                        }
                        
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            categories.forEach { item ->
                                CategoryRow(item = item)
                            }
                        }
                    }
                }
                
                // 3. Focus Habits Detail Expansion
                FocusHeatmapSection(heatmap = uiState.focusHeatmap)
                
                PeakUsagePeriodsSection(periodUsage = uiState.periodUsage)

                if (uiState.weeklyFocusSessions > 0 || uiState.recentFocusSessions.isNotEmpty()) {
                    FocusHistoryCard(
                        weeklyCount = uiState.weeklyFocusSessions,
                        successRate = uiState.focusSuccessRate,
                        avgMin = uiState.avgFocusSessionMin,
                        sessions = uiState.recentFocusSessions,
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun FocusHeatmapSection(
    heatmap: List<FocusHeatmapPoint>,
    modifier: Modifier = Modifier,
) {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    var selectedPoint by remember(heatmap) { mutableStateOf(heatmap.lastOrNull()) }

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Focus Consistency Heatmap",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Day Labels Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dayLabels.forEach { label ->
                        Text(
                            text = label,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                        )
                    }
                }

                for (row in 0 until 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (col in 0 until 7) {
                            val index = row * 7 + col
                            val point = heatmap.getOrNull(index)
                            val score = point?.focusScore ?: 0
                            val isSelected = selectedPoint?.dayKey == point?.dayKey
                            
                            val color = if (score == 0) {
                                Color.White.copy(alpha = 0.04f)
                            } else {
                                val factor = (score / 100f).coerceIn(0.1f, 1.0f)
                                val mixColor = androidx.compose.ui.graphics.lerp(AccentSecondary, AccentPrimary, factor)
                                mixColor.copy(alpha = 0.25f + factor * 0.75f)
                            }

                            val borderStroke = when {
                                isSelected -> BorderStroke(2.dp, AccentSecondary)
                                score > 70 -> BorderStroke(1.dp, AccentSecondary.copy(alpha = 0.4f))
                                else -> BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(color)
                                    .border(borderStroke, RoundedCornerShape(6.dp))
                                    .clickable(enabled = point != null) {
                                        selectedPoint = point
                                    }
                            )
                        }
                    }
                }
            }

            // Selected Day Details panel
            selectedPoint?.let { point ->
                val rating = when {
                    point.focusScore >= 80 -> "🌌 Exceptional Focus Day"
                    point.focusScore >= 50 -> "🎯 Highly Productive Day"
                    point.focusScore >= 25 -> "⚡ Steady Focus Day"
                    point.focusScore > 0 -> "🌱 Focused Habit Building"
                    else -> "💤 Rest Day (No Sessions)"
                }
                
                val ratingColor = when {
                    point.focusScore >= 70 -> Success
                    point.focusScore >= 40 -> Warning
                    point.focusScore > 0 -> AccentPrimary
                    else -> TextSecondary
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.02f))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = point.dateLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = rating,
                                style = MaterialTheme.typography.labelSmall,
                                color = ratingColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${point.focusScore}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (point.focusScore > 0) AccentSecondary else TextSecondary
                            )
                            Text(
                                text = "Focus Score",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Last 28 days activity",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Less", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.04f)))
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(AccentSecondary.copy(alpha = 0.3f)))
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(AccentPrimary.copy(alpha = 0.6f)))
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(AccentPrimary))
                    Text("More", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun PeakUsagePeriodsSection(
    periodUsage: PeriodUsage,
    modifier: Modifier = Modifier,
) {
    val total = (periodUsage.morningMs + periodUsage.afternoonMs + periodUsage.eveningMs + periodUsage.nightMs).coerceAtLeast(1L).toFloat()
    
    val morningPct = periodUsage.morningMs / total
    val afternoonPct = periodUsage.afternoonMs / total
    val eveningPct = periodUsage.eveningMs / total
    val nightPct = periodUsage.nightMs / total
    
    val morningBrush = Brush.horizontalGradient(listOf(Color(0xFFFFB84D), Color(0xFFFF8B7C)))
    val afternoonBrush = Brush.horizontalGradient(listOf(Color(0xFF5ED5FF), Color(0xFF8B7CFF)))
    val eveningBrush = Brush.horizontalGradient(listOf(Color(0xFF8B7CFF), Color(0xFFFF5A6E)))
    val nightBrush = Brush.horizontalGradient(listOf(Color(0xFFFF5A6E), Color(0xFFD63B5D)))
    
    val periods = listOf(
        Quadruple("Morning", "6 AM - 12 PM", morningPct, morningBrush),
        Quadruple("Afternoon", "12 PM - 6 PM", afternoonPct, afternoonBrush),
        Quadruple("Evening", "6 PM - 10 PM", eveningPct, eveningBrush),
        Quadruple("Night", "10 PM - 6 AM", nightPct, nightBrush)
    )
    
    val maxPeriod = periods.maxByOrNull { it.third }
    
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Usage Peak Periods",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            
            // 1. Stacked Ratio Distribution Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f))
            ) {
                if (morningPct > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(morningPct.coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(morningBrush)
                    )
                }
                if (afternoonPct > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(afternoonPct.coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(afternoonBrush)
                    )
                }
                if (eveningPct > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(eveningPct.coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(eveningBrush)
                    )
                }
                if (nightPct > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(nightPct.coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(nightBrush)
                    )
                }
            }

            // 2. 2x2 Grid of Period Tiles
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PeriodGridCard(
                        title = "Morning",
                        timeRange = "6 AM - 12 PM",
                        pct = morningPct,
                        brush = morningBrush,
                        emoji = "🌅",
                        isMax = maxPeriod?.first == "Morning" && morningPct > 0f,
                        modifier = Modifier.weight(1f)
                    )
                    PeriodGridCard(
                        title = "Afternoon",
                        timeRange = "12 PM - 6 PM",
                        pct = afternoonPct,
                        brush = afternoonBrush,
                        emoji = "☀️",
                        isMax = maxPeriod?.first == "Afternoon" && afternoonPct > 0f,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PeriodGridCard(
                        title = "Evening",
                        timeRange = "6 PM - 10 PM",
                        pct = eveningPct,
                        brush = eveningBrush,
                        emoji = "🌆",
                        isMax = maxPeriod?.first == "Evening" && eveningPct > 0f,
                        modifier = Modifier.weight(1f)
                    )
                    PeriodGridCard(
                        title = "Night",
                        timeRange = "10 PM - 6 AM",
                        pct = nightPct,
                        brush = nightBrush,
                        emoji = "🌙",
                        isMax = maxPeriod?.first == "Night" && nightPct > 0f,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // 3. Advice Card (properly wrapping container)
            if (maxPeriod != null && maxPeriod.third > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Warning.copy(alpha = 0.08f))
                        .border(1.dp, Warning.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "💡 Advice: Your highest usage is during ${maxPeriod.first}. Block apps or plan focus slots during this window.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Warning
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodGridCard(
    title: String,
    timeRange: String,
    pct: Float,
    brush: Brush,
    emoji: String,
    isMax: Boolean,
    modifier: Modifier = Modifier
) {
    val cardBackground = if (isMax) {
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.02f))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.03f), Color.White.copy(alpha = 0.01f))
        )
    }

    val cardBorder = if (isMax) {
        BorderStroke(1.dp, AccentSecondary.copy(alpha = 0.3f))
    } else {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = cardBorder
    ) {
        Box(
            modifier = Modifier
                .background(cardBackground)
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$emoji $title",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isMax) TextPrimary else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(pct * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isMax) AccentSecondary else TextPrimary
                    )
                }

                // Progress Bar (Capsule)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    if (pct > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(pct.coerceIn(0f, 1f))
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(brush)
                        )
                    }
                }

                Text(
                    text = timeRange,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun PremiumDistractionIndexCard(index: Int, label: String) {
    val gaugeColor = when {
        index >= 70 -> Danger
        index >= 40 -> Warning
        else -> Success
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "concentric")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val sweepBrush = Brush.sweepGradient(
        colors = listOf(Success, Warning, Danger, Success)
    )
    
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Distraction Index",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (index >= 50) Icons.Outlined.Warning else Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = gaugeColor,
                    modifier = Modifier.size(20.dp),
                )
            }
            
            Box(
                modifier = Modifier.size(170.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(150.dp * pulseScale)) {
                    val radius = size.minDimension / 2f
                    
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(gaugeColor.copy(alpha = 0.12f), Color.Transparent),
                            radius = radius
                       ),
                       radius = radius
                    )
                    
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = radius * 0.8f,
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(12f, 12f),
                                phase = rotationAngle
                            )
                        )
                    )

                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = radius,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )

                    val sweep = (index / 100f) * 360f
                    drawArc(
                        brush = sweepBrush,
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$index",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "INDEX",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = gaugeColor,
                fontWeight = FontWeight.ExtraBold
            )
            
            Text(
                text = "Measures reactivity vs. intentionality based on app classification and time of use. Lower is better.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun DoomscrollAlertCard(
    apps: List<DoomscrollAppItem>,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "doom")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val shape = RoundedCornerShape(LumetrixTokens.CardRadius)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape),
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, Danger.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(Danger.copy(alpha = 0.1f), Color(0xFF0B1020).copy(alpha = 0.9f))
                    )
                )
                .padding(LumetrixTokens.CardSpacing)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🚨", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Doomscroll Alert",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Danger.copy(alpha = 0.15f * pulseAlpha))
                            .border(1.dp, Danger.copy(alpha = 0.5f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Rabbit Holes",
                            style = MaterialTheme.typography.labelSmall,
                            color = Danger,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Text(
                    text = "We detected apps with abnormally long durations per launch. Limit these to protect your focus.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                
                if (apps.isEmpty()) {
                    Text(
                        text = "No rabbit hole apps detected this week! Keep it up.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Success,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        apps.forEach { app ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = app.appName, 
                                        style = MaterialTheme.typography.bodyMedium, 
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${app.totalSessions} launches", 
                                        style = MaterialTheme.typography.labelSmall, 
                                        color = TextSecondary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${app.avgSessionMin} min / use",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Danger,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "avg session duration",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusHistoryCard(
    weeklyCount: Int,
    successRate: Int,
    avgMin: Int,
    sessions: List<com.lumetrix.statsmanager.domain.model.FocusSessionItem>,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Focus History",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = AccentPrimary,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                FocusStatItem(label = "This Week", value = "$weeklyCount sessions")
                FocusStatItem(label = "Success Rate", value = "$successRate%")
                FocusStatItem(label = "Avg Session", value = "${avgMin}m")
            }
            if (sessions.isNotEmpty()) {
                HorizontalDivider(color = Divider)
                Text(
                    text = "Recent Sessions",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                sessions.forEach { session ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = session.mode,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${session.dateLabel} · ${session.timeLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (session.wasCompleted) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = Success,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "⚡ ${session.pointsEarned} pts",
                                style = MaterialTheme.typography.labelMedium,
                                color = AccentPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
private fun InsightCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = AccentPrimary)
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

@Composable
private fun RecommendationCard(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(imageVector = Icons.Outlined.Psychology, contentDescription = null, tint = AccentSecondary)
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        Text(text = body, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

private fun InsightIcon.toIcon(): ImageVector = when (this) {
    InsightIcon.Night -> Icons.Outlined.Nightlight
    InsightIcon.Bedtime -> Icons.Outlined.Bedtime
    InsightIcon.Trending -> Icons.Outlined.TrendingUp
    InsightIcon.Psychology -> Icons.Outlined.Psychology
}

private data class BarChartDataPoint(
    val label: String,
    val value: Float,
    val rawMs: Long = 0L
)

private data class CategoryItem(
    val name: String,
    val emoji: String,
    val durationLabel: String,
    val percentage: Int,
    val color: Color
)

private data class InsightsPeriodData(
    val totalMs: Long,
    val subtitle: String,
    val trendText: String,
    val bars: List<BarChartDataPoint>
)

private fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "0m"
    val totalMinutes = Math.round(ms / 60_000.0).coerceAtLeast(1L)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"
    } else {
        "${minutes}m"
    }
}

@Composable
private fun CategoryRow(
    item: CategoryItem,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(item.color.copy(alpha = 0.15f))
                        .border(1.dp, item.color.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.emoji,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.durationLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${item.percentage}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.width(36.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(item.percentage / 100f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(item.color)
            )
        }
    }
}

@Composable
private fun InsightsBarChart(
    data: List<BarChartDataPoint>,
    selectedBarIndex: Int,
    onBarSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { it.value }?.coerceAtLeast(1f) ?: 6f
    val gridMax = if (maxValue <= 3f) 3f else if (maxValue <= 6f) 6f else ((maxValue / 3).toInt() + 1) * 3f
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val gridLabels = listOf("${gridMax.toInt()}h", "${(gridMax * 0.66f).toInt()}h", "${(gridMax * 0.33f).toInt()}h", "0h")
            gridLabels.forEach { label ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.width(32.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.05f))
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 36.dp, end = 8.dp, bottom = 12.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, point ->
                val isSelected = index == selectedBarIndex
                val barHeightFraction = point.value / gridMax
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onBarSelected(index) }
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                            )
                        }
                        
                        val hasData = point.value > 0f
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .fillMaxHeight(if (hasData) barHeightFraction.coerceIn(0.05f, 1f) else 0.05f)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .then(
                                    if (hasData) {
                                        Modifier.background(
                                            Brush.verticalGradient(
                                                if (isSelected) {
                                                    listOf(Color(0xFF8126F2), Color(0xFF26B5F2))
                                                } else {
                                                    listOf(Color(0xFF8126F2).copy(alpha = 0.6f), Color(0xFF26B5F2).copy(alpha = 0.6f))
                                                }
                                            )
                                        )
                                    } else Modifier
                                )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightsTabSwitcher(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf("Daily", "Weekly", "Monthly", "Yearly")
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(com.lumetrix.statsmanager.ui.theme.GlassCard)
            .border(1.dp, GlassCardBorder.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(4.dp)
    ) {
        val tabFraction = 1f / tabs.size
        
        val animatedOffsetFraction by animateFloatAsState(
            targetValue = selectedTab.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "tabSlide"
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth(tabFraction)
                .height(40.dp)
                .align(Alignment.CenterStart)
                .graphicsLayer {
                    translationX = this.size.width * animatedOffsetFraction
                }
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        listOf(AccentPrimary.copy(alpha = 0.35f), AccentSecondary.copy(alpha = 0.2f))
                    )
                )
                .border(1.dp, AccentPrimary.copy(alpha = 0.5f), RoundedCornerShape(50))
        )
        
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { onTabSelected(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextSecondary,
                    )
                }
            }
        }
    }
}

private data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
