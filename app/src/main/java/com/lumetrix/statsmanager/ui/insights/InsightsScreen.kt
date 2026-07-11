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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Insights",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                )
                Text(
                    text = "Your personal productivity lab",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                )
            }
        }

        uiState.lastSyncedLabel?.let { label ->
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        SyncStatusText(isSyncing = uiState.isSyncing)

        Spacer(modifier = Modifier.height(16.dp))

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
            }

            AnimatedVisibility(
                visible = uiState.selectedTab == 0,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
                exit = fadeOut()
            ) {
                OverviewTabContent(uiState = uiState)
            }

            AnimatedVisibility(
                visible = uiState.selectedTab == 1,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
                exit = fadeOut()
            ) {
                FocusHabitsTabContent(uiState = uiState)
            }

            AnimatedVisibility(
                visible = uiState.selectedTab == 2,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
                exit = fadeOut()
            ) {
                HealthAnalyticsTabContent(uiState = uiState)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Sub-Views representing tab contents
// ─────────────────────────────────────────────────────────────

@Composable
private fun OverviewTabContent(
    uiState: com.lumetrix.statsmanager.domain.model.InsightsUiState
) {
    Column(verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)) {
        if (uiState.hasUsageAccess) {
            val totalMs = (uiState.weeklyScreenTimeHours.sum() * 60 * 60 * 1000).toLong()
            InsightsComparisonGrid(
                screenTimeMs = totalMs,
                screenTimeChangePercent = uiState.screenTimeChangePercent,
                focusScore = 74, // Weekly baseline placeholder
                focusScoreChange = uiState.habitScoreChangePercent,
                focusPoints = uiState.focusPointsEarned
            )
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Weekly Analytics",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                NeonLineChart(
                    data = uiState.weeklyScreenTimeHours.ifEmpty {
                        listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
                    }.mapIndexed { index, value ->
                        val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        com.lumetrix.statsmanager.domain.model.ChartDataPoint(
                            dayLabel = labels.getOrElse(index) { "${index + 1}" },
                            value = value,
                            formattedLabel = if (value > 0f) {
                                val totalMinutes = (value * 60).toInt()
                                if (totalMinutes >= 60) "${totalMinutes / 60}h ${totalMinutes % 60}m" else "${totalMinutes}m"
                            } else "0m"
                        )
                    },
                )
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Productivity Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                DonutChart(
                    segments = listOf(
                        DonutSegment("Productive", uiState.productivePercent.toFloat(), Success),
                        DonutSegment("Neutral", uiState.neutralPercent.toFloat(), Warning),
                        DonutSegment("Distracting", uiState.distractingPercent.toFloat(), Danger),
                    ),
                )
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun FocusHabitsTabContent(
    uiState: com.lumetrix.statsmanager.domain.model.InsightsUiState
) {
    Column(verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)) {
        if (uiState.hasUsageAccess) {
            FocusHeatmapSection(heatmap = uiState.focusHeatmap)
            
            PeakUsagePeriodsSection(periodUsage = uiState.periodUsage)
        }

        if (uiState.hasUsageAccess && (uiState.weeklyFocusSessions > 0 || uiState.recentFocusSessions.isNotEmpty())) {
            FocusHistoryCard(
                weeklyCount = uiState.weeklyFocusSessions,
                successRate = uiState.focusSuccessRate,
                avgMin = uiState.avgFocusSessionMin,
                sessions = uiState.recentFocusSessions,
            )
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun HealthAnalyticsTabContent(
    uiState: com.lumetrix.statsmanager.domain.model.InsightsUiState
) {
    Column(verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)) {
        if (uiState.hasUsageAccess) {
            PremiumDistractionIndexCard(
                index = uiState.distractionIndex,
                label = uiState.distractionIndexLabel
            )

            DoomscrollAlertCard(apps = uiState.doomscrollApps)
        }

        Text(
            text = "Behavioral Insights",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        uiState.behavioralInsights.forEach { insight ->
            InsightCard(
                icon = insight.iconKey.toIcon(),
                title = insight.title,
                subtitle = insight.subtitle,
            )
        }

        Text(
            text = "Recommendations",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        uiState.recommendations.forEachIndexed { index, recommendation ->
            if (index == 0) {
                GradientGlassCard(modifier = Modifier.fillMaxWidth()) {
                    RecommendationCard(
                        title = recommendation.title,
                        body = recommendation.body,
                    )
                }
            } else {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    RecommendationCard(
                        title = recommendation.title,
                        body = recommendation.body,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

// ─────────────────────────────────────────────────────────────
// Sub-Components
// ─────────────────────────────────────────────────────────────

@Composable
private fun InsightsTabSwitcher(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf("Overview", "Focus Habits", "Health")
    
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

@Composable
private fun InsightsComparisonGrid(
    screenTimeMs: Long,
    screenTimeChangePercent: Int,
    focusScore: Int,
    focusScoreChange: Int,
    focusPoints: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Screen Time Tile
        Box(modifier = Modifier.weight(1f)) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowBorder = screenTimeChangePercent < 0 // Glow if user decreased screen time
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Screen Time", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    Text(DurationFormatter.formatShort(screenTimeMs), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    
                    val isGood = screenTimeChangePercent < 0
                    val color = if (isGood) Success else Danger
                    val sign = if (screenTimeChangePercent > 0) "+" else ""
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isGood) "↓" else "↑",
                            style = MaterialTheme.typography.labelMedium,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "$sign$screenTimeChangePercent%",
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        // Focus Score Tile
        Box(modifier = Modifier.weight(1f)) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowBorder = focusScoreChange >= 0
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Focus Score", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    Text("$focusScore", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    
                    val isGood = focusScoreChange >= 0
                    val color = if (isGood) Success else Danger
                    val sign = if (focusScoreChange > 0) "+" else ""
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isGood) "▲" else "▼",
                            style = MaterialTheme.typography.labelSmall,
                            color = color
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "$sign$focusScoreChange pts",
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        // Focus Points Tile
        Box(modifier = Modifier.weight(1f)) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowBorder = true
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Focus Points", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("⚡", style = MaterialTheme.typography.titleMedium)
                        Text("$focusPoints", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentSecondary)
                    }
                    Text("Balance", style = MaterialTheme.typography.labelSmall, color = Success, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun FocusHeatmapSection(
    heatmap: List<FocusHeatmapPoint>,
    modifier: Modifier = Modifier,
) {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

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
                            
                            val color = if (score == 0) {
                                Color.White.copy(alpha = 0.04f)
                            } else {
                                val factor = (score / 100f).coerceIn(0.1f, 1.0f)
                                val mixColor = androidx.compose.ui.graphics.lerp(AccentSecondary, AccentPrimary, factor)
                                mixColor.copy(alpha = 0.25f + factor * 0.75f)
                            }

                            val borderStroke = if (score > 70) {
                                BorderStroke(1.dp, AccentSecondary.copy(alpha = 0.4f))
                            } else {
                                BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(color)
                                    .border(borderStroke, RoundedCornerShape(6.dp))
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
        Quadruple("🌅 Morning", "6 AM - 12 PM", morningPct, morningBrush),
        Quadruple("☀️ Afternoon", "12 PM - 6 PM", afternoonPct, afternoonBrush),
        Quadruple("🌆 Evening", "6 PM - 10 PM", eveningPct, eveningBrush),
        Quadruple("🌙 Night", "10 PM - 6 AM", nightPct, nightBrush)
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
            
            periods.forEach { (title, subtitle, pct, brush) ->
                val isMax = title == maxPeriod?.first && pct > 0f
                val trackColor = Color.White.copy(alpha = 0.05f)
                
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = title, 
                                style = MaterialTheme.typography.bodyMedium, 
                                color = TextPrimary, 
                                fontWeight = if (isMax) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Text(
                            text = "${(pct * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isMax) Warning else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(trackColor)
                            .padding(end = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (pct > 0f) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(pct.coerceIn(0f, 1f))
                                    .height(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(10.dp)
                                        .clip(CircleShape)
                                        .background(brush)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(2.dp, AccentSecondary, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
            
            if (maxPeriod != null && maxPeriod.third > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Warning.copy(alpha = 0.1f))
                        .border(1.dp, Warning.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "💡 Advice: Your highest usage is during ${maxPeriod.first.substring(2)}. Block apps or plan focus slots during this window.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Warning
                    )
                }
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

private data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
