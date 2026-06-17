package com.lumetrix.statsmanager.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lumetrix.statsmanager.ui.components.AppUsageCard
import com.lumetrix.statsmanager.ui.components.FocusScoreOrb
import com.lumetrix.statsmanager.ui.components.GlassCard
import com.lumetrix.statsmanager.ui.components.GradientGlassCard
import com.lumetrix.statsmanager.ui.components.NeonLineChart
import com.lumetrix.statsmanager.ui.components.StatCard
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.Danger
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary
import com.lumetrix.statsmanager.ui.theme.Warning

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    userName: String = "Alex",
    greeting: String = "Good Evening",
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = LumetrixTokens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                )
            }
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = AccentSecondary.copy(alpha = 0.15f),
            ) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = userName.first().uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = AccentSecondary,
                    )
                }
            }
        }

        FocusScoreOrb(
            score = 84,
            label = "Focus Score",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )

        GradientGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "AI Insight",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentSecondary,
                )
                Text(
                    text = "Your focus improved 18% this week",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                )
                Text(
                    text = "Peak productivity detected between 9–11 AM",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Screen Time",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                NeonLineChart(
                    data = listOf(2.1f, 3.4f, 2.8f, 4.2f, 3.6f, 5.1f, 4.4f),
                )
            }
        }

        Text(
            text = "Top Apps",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )

        AppUsageCard(
            appName = "Instagram",
            duration = "2h 14m",
            category = "Distracting",
            categoryColor = Danger,
        )
        AppUsageCard(
            appName = "Notion",
            duration = "1h 42m",
            category = "Productive",
            categoryColor = Success,
        )
        AppUsageCard(
            appName = "Spotify",
            duration = "58m",
            category = "Neutral",
            categoryColor = Warning,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing),
        ) {
            StatCard(
                label = "Unlocks",
                value = "47",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Notifications",
                value = "128",
                modifier = Modifier.weight(1f),
                accent = AccentSecondary,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing),
        ) {
            StatCard(
                label = "Pickups",
                value = "63",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Focus Time",
                value = "3h 12m",
                modifier = Modifier.weight(1f),
                accent = Success,
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
