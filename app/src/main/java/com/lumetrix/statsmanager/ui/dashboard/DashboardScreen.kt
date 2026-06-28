package com.lumetrix.statsmanager.ui.dashboard

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumetrix.statsmanager.domain.model.AppCategory
import com.lumetrix.statsmanager.domain.model.AppUsageItem
import com.lumetrix.statsmanager.ui.components.AppUsageCard
import com.lumetrix.statsmanager.ui.components.GlassCard
import com.lumetrix.statsmanager.ui.components.GradientGlassCard
import com.lumetrix.statsmanager.ui.components.NeonLineChart
import com.lumetrix.statsmanager.ui.components.StatCard
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToAppDetails: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isVisible by remember { mutableStateOf(false) }
    
    var selectedApp by remember { mutableStateOf<AppUsageItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isVisible = true
        }
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

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { 50 },
                animationSpec = tween(600)
            ) + fadeIn(animationSpec = tween(600))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = LumetrixTokens.ScreenPadding),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = LumetrixTokens.CardSpacing),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = uiState.greeting,
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                        )
                        SyncStatusText(isSyncing = uiState.isSyncing)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing),
                ) {
                    if (!uiState.hasUsageAccess) {
                        UsageAccessBanner(onGrantClick = { viewModel.openUsageAccessSettings(context) })
                    }

                    // 2x2 Stats grid — moved to top for instant visibility
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing),
                    ) {
                        StatCard(
                            label = "Focus Score",
                            value = uiState.focusScore.toString(),
                            modifier = Modifier.weight(1f),
                            accent = Warning,
                        )
                        StatCard(
                            label = "Screen Time",
                            value = uiState.totalScreenTimeLabel,
                            modifier = Modifier.weight(1f),
                            accent = AccentPrimary,
                        )
                    }

                    // 2. Contextual Nudge
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

                    // 2.5 Weekly Screen Time Graph
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Weekly Trend",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                )
                            }
                            NeonLineChart(
                                data = uiState.weeklyScreenTimeHours.ifEmpty {
                                    listOf(
                                        com.lumetrix.statsmanager.domain.model.ChartDataPoint("Mon", 0f),
                                        com.lumetrix.statsmanager.domain.model.ChartDataPoint("Tue", 0f),
                                        com.lumetrix.statsmanager.domain.model.ChartDataPoint("Wed", 0f),
                                        com.lumetrix.statsmanager.domain.model.ChartDataPoint("Thu", 0f),
                                        com.lumetrix.statsmanager.domain.model.ChartDataPoint("Fri", 0f),
                                        com.lumetrix.statsmanager.domain.model.ChartDataPoint("Sat", 0f),
                                        com.lumetrix.statsmanager.domain.model.ChartDataPoint("Sun", 0f)
                                    )
                                },
                            )
                        }
                    }

                    // 3. Quick Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing),
                    ) {
                        StatCard(
                            label = "Unlocks",
                            value = uiState.unlockCount.toString(),
                            modifier = Modifier.weight(1f),
                            accent = Success
                        )
                        StatCard(
                            label = "Notifications",
                            value = uiState.notificationCount.toString(),
                            modifier = Modifier.weight(1f),
                            accent = AccentSecondary,
                        )
                    }

                    // 4. Category Breakdown Bar
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "Time Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                            )
                            
                            val totalMs = uiState.productiveMs + uiState.neutralMs + uiState.distractingMs
                            val prodWeight = if (totalMs > 0) (uiState.productiveMs.toFloat() / totalMs) else 0.33f
                            val neutralWeight = if (totalMs > 0) (uiState.neutralMs.toFloat() / totalMs) else 0.33f
                            val distWeight = if (totalMs > 0) (uiState.distractingMs.toFloat() / totalMs) else 0.33f

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                if (prodWeight > 0f) {
                                    Box(modifier = Modifier.weight(prodWeight).fillMaxHeight().background(Success))
                                }
                                if (neutralWeight > 0f) {
                                    Box(modifier = Modifier.weight(neutralWeight).fillMaxHeight().background(Warning))
                                }
                                if (distWeight > 0f) {
                                    Box(modifier = Modifier.weight(distWeight).fillMaxHeight().background(Danger))
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Productive", style = MaterialTheme.typography.labelMedium, color = Success)
                                    Text(uiState.productiveLabel, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Neutral", style = MaterialTheme.typography.labelMedium, color = Warning)
                                    Text(uiState.neutralLabel, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Distracting", style = MaterialTheme.typography.labelMedium, color = Danger)
                                    Text(uiState.distractingLabel, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                                }
                            }
                        }
                    }

                    // 5. Top Apps
                    Text(
                        text = "Top Apps",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )

                    if (uiState.topApps.isEmpty()) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (uiState.hasUsageAccess) "No app usage recorded yet today." else "Grant usage access to see apps.",
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
                                onClick = { selectedApp = app }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }

        if (selectedApp != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedApp = null },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(LumetrixTokens.ScreenPadding)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = selectedApp?.appName ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    
                    TextButton(
                        onClick = {
                            selectedApp?.packageName?.let { onNavigateToAppDetails(it) }
                            selectedApp = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Detailed Stats", color = AccentPrimary)
                    }

                    TextButton(
                        onClick = {
                            selectedApp?.packageName?.let { viewModel.cycleAppCategory(it) }
                            selectedApp = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Category", color = TextSecondary)
                    }
                    
                    TextButton(
                        onClick = {
                            Toast.makeText(context, "Added to Focus list (Coming Soon)", Toast.LENGTH_SHORT).show()
                            selectedApp = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add to Focus Blocker", color = TextSecondary)
                    }
                }
            }
        }
    }
}
