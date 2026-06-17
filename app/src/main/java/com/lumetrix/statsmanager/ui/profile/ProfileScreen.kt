package com.lumetrix.statsmanager.ui.profile

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lumetrix.statsmanager.ui.components.GlassCard
import com.lumetrix.statsmanager.ui.components.GradientGlassCard
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.Divider
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary
import com.lumetrix.statsmanager.ui.theme.Warning

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    userName: String = "Alex",
    title: String = "Deep Worker",
    level: Int = 12,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = LumetrixTokens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        GradientGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = AccentPrimary.copy(alpha = 0.2f),
                ) {
                    androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userName.first().uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = AccentPrimary,
                        )
                    }
                }
                Column {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentSecondary,
                    )
                    Text(
                        text = "Level $level",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }
        }

        Text(
            text = "Achievements",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )

        AchievementCard(
            title = "Focus Streak",
            subtitle = "7 days of consistent focus",
            accent = Success,
        )
        AchievementCard(
            title = "Night Guardian",
            subtitle = "Reduced late-night usage by 30%",
            accent = AccentSecondary,
        )
        AchievementCard(
            title = "Deep Worker",
            subtitle = "Completed 50 focus sessions",
            accent = Warning,
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Lifetime Stats",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LifetimeStatRow(label = "Focus hours", value = "128h")
                HorizontalDivider(color = Divider)
                LifetimeStatRow(label = "Days tracked", value = "94")
                HorizontalDivider(color = Divider)
                LifetimeStatRow(label = "Distraction reduction", value = "32%")
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun AchievementCard(
    title: String,
    subtitle: String,
    accent: androidx.compose.ui.graphics.Color,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowBorder = true,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
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
private fun LifetimeStatRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )
    }
}
