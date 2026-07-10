package com.lumetrix.statsmanager.ui.insights

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumetrix.statsmanager.domain.model.InsightIcon
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
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary
import com.lumetrix.statsmanager.ui.theme.Warning

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
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
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

        Text(
            text = "Insights",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
        )
        Text(
            text = "Your personal productivity laboratory",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
        )
        uiState.lastSyncedLabel?.let { label ->
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
        SyncStatusText(isSyncing = uiState.isSyncing)

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

        Text(
            text = "Behavioral Insights",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )

        uiState.behavioralInsights.forEach { insight ->
            InsightCard(
                icon = insight.iconKey.toIcon(),
                title = insight.title,
                subtitle = insight.subtitle,
            )
        }

        // Feature 4: Distraction Index Card
        if (uiState.hasUsageAccess) {
            DistractionIndexCard(
                index = uiState.distractionIndex,
                label = uiState.distractionIndexLabel,
            )
        }

        // Feature 7: Focus Session History
        if (uiState.hasUsageAccess && (uiState.weeklyFocusSessions > 0 || uiState.recentFocusSessions.isNotEmpty())) {
            FocusHistoryCard(
                weeklyCount = uiState.weeklyFocusSessions,
                successRate = uiState.focusSuccessRate,
                avgMin = uiState.avgFocusSessionMin,
                sessions = uiState.recentFocusSessions,
            )
        }

        Text(
            text = "Recommendations",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
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
        } // Close inner scroll column
    } // Close outer column
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
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

@Composable
private fun RecommendationCard(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(imageVector = Icons.Outlined.Psychology, contentDescription = null, tint = AccentSecondary)
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text(text = body, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

/** Feature 4: Distraction Index gauge card */
@Composable
private fun DistractionIndexCard(index: Int, label: String) {
    val gaugeColor = when {
        index >= 70 -> Danger
        index >= 40 -> Warning
        else -> Success
    }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Distraction Index",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "$index",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = gaugeColor,
                    )
                    Text(
                        text = "/ 100",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Icon(
                        imageVector = if (index >= 50) Icons.Outlined.Warning else Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = gaugeColor,
                        modifier = Modifier.size(32.dp),
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        color = gaugeColor,
                    )
                }
            }
            Text(
                text = "Measures how reactive vs. intentional your phone usage is this week. Lower is better.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary.copy(alpha = 0.7f),
            )
        }
    }
}

/** Feature 7: Focus History card showing Pomodoro session stats */
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
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Focus History",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
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

private fun InsightIcon.toIcon(): ImageVector = when (this) {
    InsightIcon.Night -> Icons.Outlined.Nightlight
    InsightIcon.Bedtime -> Icons.Outlined.Bedtime
    InsightIcon.Trending -> Icons.Outlined.TrendingUp
    InsightIcon.Psychology -> Icons.Outlined.Psychology
}
