package com.lumetrix.statsmanager.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
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

private fun InsightIcon.toIcon(): ImageVector = when (this) {
    InsightIcon.Night -> Icons.Outlined.Nightlight
    InsightIcon.Bedtime -> Icons.Outlined.Bedtime
    InsightIcon.Trending -> Icons.Outlined.TrendingUp
    InsightIcon.Psychology -> Icons.Outlined.Psychology
}
