package com.lumetrix.statsmanager.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lumetrix.statsmanager.ui.components.DonutChart
import com.lumetrix.statsmanager.ui.components.DonutSegment
import com.lumetrix.statsmanager.ui.components.GlassCard
import com.lumetrix.statsmanager.ui.components.GradientGlassCard
import com.lumetrix.statsmanager.ui.components.NeonLineChart
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.Danger
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary
import com.lumetrix.statsmanager.ui.theme.Warning

@Composable
fun InsightsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = LumetrixTokens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing),
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

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Weekly Analytics",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                NeonLineChart(
                    data = listOf(3.2f, 4.1f, 3.8f, 5.0f, 4.6f, 3.9f, 4.8f),
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
                        DonutSegment("Productive", 52f, Success),
                        DonutSegment("Neutral", 28f, Warning),
                        DonutSegment("Distracting", 20f, Danger),
                    ),
                )
            }
        }

        Text(
            text = "Behavioral Insights",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )

        InsightCard(
            icon = Icons.Outlined.Nightlight,
            title = "You scroll more after 10 PM",
            subtitle = "Late-night usage increased 24% this week",
        )
        InsightCard(
            icon = Icons.Outlined.Bedtime,
            title = "Peak distraction at 11:42 PM",
            subtitle = "Consider enabling wind-down focus mode",
        )

        Text(
            text = "AI Recommendations",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )

        GradientGlassCard(modifier = Modifier.fillMaxWidth()) {
            RecommendationCard(
                title = "Reduce social media",
                body = "Limit Instagram to 45 minutes daily to improve focus score.",
            )
        }
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            RecommendationCard(
                title = "Optimize focus hours",
                body = "Schedule deep work between 9–11 AM when productivity peaks.",
            )
        }
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            RecommendationCard(
                title = "Improve sleep timing",
                body = "Wind down screen usage 30 minutes earlier for better recovery.",
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentPrimary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun RecommendationCard(
    title: String,
    body: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            imageVector = Icons.Outlined.Psychology,
            contentDescription = null,
            tint = AccentSecondary,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
    }
}
