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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumetrix.statsmanager.ui.components.AppUsageCard
import com.lumetrix.statsmanager.ui.components.FocusScoreOrb
import com.lumetrix.statsmanager.ui.components.GlassCard
import com.lumetrix.statsmanager.ui.components.GradientGlassCard
import com.lumetrix.statsmanager.ui.components.NeonLineChart
import com.lumetrix.statsmanager.ui.components.StatCard
import com.lumetrix.statsmanager.ui.permissions.SyncStatusText
import com.lumetrix.statsmanager.ui.permissions.UsageAccessBanner
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
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
                    text = uiState.greeting,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                )
                Text(
                    text = uiState.userName,
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                )
                SyncStatusText(isSyncing = uiState.isSyncing)
                uiState.lastSyncedLabel?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }
                uiState.syncError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.labelSmall,
                        color = com.lumetrix.statsmanager.ui.theme.Danger,
                    )
                }
            }
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = AccentSecondary.copy(alpha = 0.15f),
            ) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.userName.first().uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = AccentSecondary,
                    )
                }
            }
        }

        if (!uiState.hasUsageAccess) {
            UsageAccessBanner(onGrantClick = { viewModel.openUsageAccessSettings(context) })
        }

        FocusScoreOrb(
            score = uiState.focusScore,
            label = "Focus Score",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )

        GradientGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Insight",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentSecondary,
                )
                Text(
                    text = uiState.insightTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                )
                Text(
                    text = uiState.insightSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Screen Time",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Text(
                        text = uiState.totalScreenTimeLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = AccentSecondary,
                    )
                }
                NeonLineChart(
                    data = uiState.weeklyScreenTimeHours.ifEmpty {
                        listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
                    },
                )
            }
        }

        Text(
            text = "Top Apps",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )

        if (uiState.topApps.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (uiState.hasUsageAccess) {
                        "No app usage recorded yet today."
                    } else {
                        "Grant usage access to see your top apps."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        } else {
            uiState.topApps.forEach { app ->
                AppUsageCard(
                    appName = app.appName,
                    duration = app.durationLabel,
                    category = app.category.label,
                    categoryColor = app.categoryColor,
                    onCategoryClick = { viewModel.cycleAppCategory(app.packageName) },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing),
        ) {
            StatCard(
                label = "Unlocks",
                value = uiState.unlockCount.toString(),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Notifications",
                value = uiState.notificationCount.toString(),
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
                value = uiState.pickupCount.toString(),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Focus Time",
                value = uiState.focusTimeLabel,
                modifier = Modifier.weight(1f),
                accent = Success,
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
