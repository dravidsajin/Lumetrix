package com.lumetrix.statsmanager.ui.focus

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumetrix.statsmanager.domain.model.AppChainRule
import com.lumetrix.statsmanager.domain.model.FocusSessionItem
import com.lumetrix.statsmanager.domain.model.FocusState
import com.lumetrix.statsmanager.domain.model.FocusUiState
import com.lumetrix.statsmanager.ui.components.GlassCard
import com.lumetrix.statsmanager.ui.components.PremiumPillButton
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.Danger
import com.lumetrix.statsmanager.ui.theme.GlassCard
import com.lumetrix.statsmanager.ui.theme.GlassCardBorder
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary

// ─────────────────────────────────────────────────────────────────────────────
// Data
// ─────────────────────────────────────────────────────────────────────────────

private data class FocusModeConfig(
    val name: String,
    val icon: String,
    val accentColor: Color,
    val bgColor: Color,
    val description: String,
)

private val availableDurations = listOf(10, 15, 25, 45, 60, 90)

private val focusModes = listOf(
    FocusModeConfig("Deep Work", "🌙", Color(0xFF8126F2), Color(0xFF1E1A3C),
        "Eliminate distractions. Achieve peak concentration on complex, meaningful tasks."),
    FocusModeConfig("Study", "🎓", Color(0xFF2196F3), Color(0xFF152A4A),
        "Learn deeply and retain information with structured, distraction-free focus."),
    FocusModeConfig("Reading", "📖", Color(0xFF00E676), Color(0xFF1A3B2A),
        "Immerse yourself in your reading without interruptions."),
    FocusModeConfig("Workout", "💪", Color(0xFFFFB74D), Color(0xFF3E2D1A),
        "Stay in the zone. Block apps and give 100% to your session."),
    FocusModeConfig("Sleep", "😴", Color(0xFF9C27B0), Color(0xFF1D1B44),
        "Wind down and let your mind rest. Block all distractions."),
    FocusModeConfig("Driving", "🚗", Color(0xFF00B0FF), Color(0xFF143547),
        "Eyes on the road. Silence everything until you arrive."),
    FocusModeConfig("Meeting", "👥", Color(0xFFD7CCC8), Color(0xFF382A24),
        "Be present. Minimize notifications during important discussions."),
    FocusModeConfig("Gaming", "🎮", Color(0xFFFF5252), Color(0xFF3E1C27),
        "Get in the flow state. No interruptions, just pure performance."),
)

// ─────────────────────────────────────────────────────────────────────────────
// Root Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FocusScreen(
    modifier: Modifier = Modifier,
    viewModel: FocusViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedModeDetail by remember { mutableStateOf<FocusModeConfig?>(null) }

    LaunchedEffect(selectedModeDetail) {
        selectedModeDetail?.let { viewModel.selectMode(it.name) }
    }
    LaunchedEffect(uiState.state) {
        if (uiState.state == FocusState.Setup) selectedModeDetail = null
    }

    when (uiState.state) {
        FocusState.Setup -> {
            if (selectedModeDetail == null) {
                FocusModeSelector(
                    uiState = uiState,
                    onModeSelected = { selectedModeDetail = it },
                    onAddRule = { viewModel.setAddRuleDialogVisible(true) },
                    modifier = modifier,
                )
            } else {
                FocusTimerDashboard(
                    config = selectedModeDetail!!,
                    uiState = uiState,
                    isActive = false,
                    onBack = { selectedModeDetail = null },
                    onStart = { viewModel.startSession() },
                    onReset = { viewModel.selectDuration(uiState.selectedDurationMinutes) },
                    onDurationSelected = { viewModel.selectDuration(it) },
                    onOpenSettings = { viewModel.setAddRuleDialogVisible(true) },
                    onAddRule = { viewModel.setAddRuleDialogVisible(true) },
                    onDeleteRule = { viewModel.deleteChainRule(it) },
                    modifier = modifier,
                )
            }
        }
        FocusState.Active, FocusState.Paused -> {
            val activeConfig = focusModes.firstOrNull { it.name == uiState.selectedMode } ?: focusModes.first()
            FocusTimerDashboard(
                config = activeConfig,
                uiState = uiState,
                isActive = true,
                onBack = { viewModel.endSession() },
                onStart = {
                    if (uiState.state == FocusState.Active) {
                        viewModel.pauseSession()
                    } else {
                        viewModel.resumeSession()
                    }
                },
                onReset = {},
                onDurationSelected = {},
                onOpenSettings = { viewModel.setAddRuleDialogVisible(true) },
                onAddRule = { viewModel.setAddRuleDialogVisible(true) },
                onDeleteRule = { viewModel.deleteChainRule(it) },
                modifier = modifier,
            )
        }
        FocusState.Completed -> FocusCompletedScreen(
            modifier = modifier,
            uiState = uiState,
            onDismiss = {
                viewModel.dismissCompleted()
                selectedModeDetail = null
            }
        )
    }

    if (uiState.showAddRuleDialog) {
        AppChainRuleDialog(
            availableApps = uiState.availableApps,
            onDismiss = { viewModel.setAddRuleDialogVisible(false) },
            onConfirm = { gateApp, durationMin, targetApp ->
                viewModel.addChainRule(
                    gatePackage = gateApp.packageName,
                    gateAppName = gateApp.appName,
                    gateDurationMin = durationMin,
                    targetPackage = targetApp.packageName,
                    targetAppName = targetApp.appName,
                )
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen 1 — Premium Mode Selector
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FocusModeSelector(
    uiState: FocusUiState,
    onModeSelected: (FocusModeConfig) -> Unit,
    onAddRule: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val selectedMode = focusModes[selectedIndex]

    // Pulsing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "heroGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "glowAlpha",
    )

    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 120.dp),
    ) {

        // ── Header ──────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LumetrixTokens.ScreenPadding)
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Focus",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                    )
                    Text(
                        text = "Choose your focus mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uiState.distractingAppsCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Danger.copy(alpha = 0.12f))
                                .border(1.dp, Danger.copy(alpha = 0.3f), RoundedCornerShape(50))
                                .padding(vertical = 4.dp, horizontal = 10.dp),
                        ) {
                            Icon(
                                Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = Danger,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                "${uiState.distractingAppsCount} distracting",
                                style = MaterialTheme.typography.labelSmall,
                                color = Danger,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFFFB84D).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFFFB84D).copy(alpha = 0.3f), RoundedCornerShape(50))
                            .padding(vertical = 4.dp, horizontal = 10.dp),
                    ) {
                        Text("⚡", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "${uiState.focusPointsBalance} FP",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFFB84D),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        // ── Hero Card ────────────────────────────────────────────────────────
        item {
            val todayModeMin = uiState.recentSessions
                .filter { it.dateLabel == "Today" && it.mode == selectedMode.name }
                .sumOf { it.actualDurationMin }

            AnimatedContent(
                targetState = selectedIndex,
                transitionSpec = {
                    (fadeIn(tween(260)) + slideInHorizontally { if (targetState > initialState) 60 else -60 })
                        .togetherWith(fadeOut(tween(180)) + slideOutHorizontally { if (targetState > initialState) -60 else 60 })
                },
                label = "heroCard",
            ) { idx ->
                val mode = focusModes[idx]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LumetrixTokens.ScreenPadding, vertical = 12.dp)
                        .height(300.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    mode.accentColor.copy(alpha = 0.35f),
                                    mode.bgColor,
                                    Color(0xFF0D0D14),
                                ),
                                center = Offset(0.3f, 0.2f),
                                radius = 900f,
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(mode.accentColor.copy(alpha = 0.5f), Color.Transparent, mode.accentColor.copy(alpha = 0.2f))
                            ),
                            RoundedCornerShape(28.dp),
                        ),
                ) {
                    // Subtle ambient glow circle in background
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 40.dp, y = (-40).dp)
                            .clip(CircleShape)
                            .background(mode.accentColor.copy(alpha = glowAlpha * 0.2f))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // Icon + Mode name
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Glowing icon pill
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(mode.accentColor.copy(alpha = glowAlpha * 0.4f))
                                    .border(1.5.dp, mode.accentColor.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(mode.icon, fontSize = 34.sp)
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = mode.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary,
                                )
                                Text(
                                    text = mode.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    lineHeight = 20.sp,
                                )
                            }
                        }

                        // Bottom chips row
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (todayModeMin > 0) {
                                StatChip(
                                    label = "Today",
                                    value = formatFocusMinutes(todayModeMin),
                                    color = mode.accentColor,
                                )
                            }
                            if (uiState.weeklyCompletedSessions > 0) {
                                StatChip(
                                    label = "Week",
                                    value = "${uiState.weeklyCompletedSessions} done",
                                    color = Success,
                                )
                            }
                            if (uiState.weeklySuccessRate > 0) {
                                StatChip(
                                    label = "Success",
                                    value = "${uiState.weeklySuccessRate}%",
                                    color = AccentSecondary,
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Mode Chip Selector ───────────────────────────────────────────────
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = LumetrixTokens.ScreenPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                itemsIndexed(focusModes) { index, mode ->
                    val isSelected = index == selectedIndex
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isSelected) mode.accentColor.copy(alpha = 0.85f)
                                else Color.White.copy(alpha = 0.05f)
                            )
                            .border(
                                1.dp,
                                if (isSelected) mode.accentColor else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(50),
                            )
                            .clickable { selectedIndex = index }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                    ) {
                        Text(mode.icon, fontSize = 14.sp)
                        Text(
                            mode.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else TextSecondary,
                        )
                    }
                }
                item {
                    // Add Rule chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AccentPrimary.copy(alpha = 0.08f))
                            .border(1.dp, AccentPrimary.copy(alpha = 0.25f), RoundedCornerShape(50))
                            .clickable { onAddRule() }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                    ) {
                        Icon(Icons.Outlined.Add, null, tint = AccentPrimary, modifier = Modifier.size(14.dp))
                        Text(
                            "Chain Rule",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentPrimary,
                        )
                    }
                }
            }
        }

        // ── Dot Indicators ──────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                focusModes.forEachIndexed { index, mode ->
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 8.dp else 5.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) mode.accentColor
                                else Color.White.copy(alpha = 0.2f)
                            )
                    )
                }
            }
        }

        // ── Start Session Button ─────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LumetrixTokens.ScreenPadding, vertical = 20.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                selectedMode.accentColor,
                                selectedMode.accentColor.copy(alpha = 0.7f),
                            )
                        )
                    )
                    .clickable { onModeSelected(selectedMode) },
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(selectedMode.icon, fontSize = 20.sp)
                    Text(
                        text = "Start ${selectedMode.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }

        // ── Today's Stats Strip ──────────────────────────────────────────────
        if (uiState.todayFocusMinutes > 0 || uiState.todaySessionCount > 0) {
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LumetrixTokens.ScreenPadding),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        FocusStatPill("Today's Focus", formatFocusMinutes(uiState.todayFocusMinutes), AccentPrimary)
                        Box(Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.08f)).align(Alignment.CenterVertically))
                        FocusStatPill("Sessions", "${uiState.todaySessionCount}", Success)
                        Box(Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.08f)).align(Alignment.CenterVertically))
                        FocusStatPill("Success", "${uiState.todaySuccessRate}%", AccentSecondary)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        // ── Chain Rules Section ──────────────────────────────────────────────
        if (uiState.chainRules.isNotEmpty()) {
            item {
                AppChainRulesSection(
                    rules = uiState.chainRules,
                    onAddRule = onAddRule,
                    onDeleteRule = { /* can't delete from grid, navigate to timer */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LumetrixTokens.ScreenPadding),
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        // ── Session History ──────────────────────────────────────────────────
        if (uiState.recentSessions.isNotEmpty()) {
            item {
                SessionHistorySection(
                    sessions = uiState.recentSessions.take(5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LumetrixTokens.ScreenPadding),
                )
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = "· $label",
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.6f),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen 2 — Timer Dashboard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FocusTimerDashboard(
    config: FocusModeConfig,
    uiState: FocusUiState,
    isActive: Boolean,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onReset: () -> Unit,
    onDurationSelected: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    onAddRule: () -> Unit,
    onDeleteRule: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LumetrixTokens.ScreenPadding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(config.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                if (isActive) {
                    Text("Session in progress", style = MaterialTheme.typography.labelSmall, color = config.accentColor)
                }
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Outlined.Shield, "Settings", tint = TextPrimary)
            }
        }

        // Duration picker — setup only
        if (!isActive) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                availableDurations.forEach { minutes ->
                    val isSelected = uiState.selectedDurationMinutes == minutes
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) config.accentColor else Color.White.copy(alpha = 0.04f))
                            .border(1.dp,
                                if (isSelected) config.accentColor.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(50))
                            .clickable { onDurationSelected(minutes) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = when {
                                minutes % 60 == 0 -> "${minutes / 60}h"
                                minutes > 60 -> "${minutes / 60}h ${minutes % 60}m"
                                else -> "${minutes}m"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        // Circular countdown ring
        val totalTime = uiState.selectedDurationMinutes * 60 * 1000f
        val progress = if (isActive) (uiState.remainingTimeMillis / totalTime).coerceIn(0f, 1f) else 1f

        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
            label = "pulseAlpha",
        )

        Box(modifier = Modifier.fillMaxWidth().height(230.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(200.dp)) {
                val strokeWidth = 8.dp.toPx()
                drawArc(Color.White.copy(alpha = 0.04f), 135f, 270f, false, style = Stroke(strokeWidth, cap = StrokeCap.Round))
                val gradient = Brush.linearGradient(
                    colors = listOf(config.accentColor, config.accentColor.copy(alpha = if (isActive) pulseAlpha else 1f), Color(0xFF00E5FF)),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, 0f),
                )
                drawArc(gradient, 135f, progress * 270f, false, style = Stroke(strokeWidth, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(config.icon, fontSize = 28.sp)
                Text(uiState.remainingTimeLabel, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text(
                    if (isActive) "You're doing great! 💪" else "Focus on your goal 🌿",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) Color(0xFF00E5FF) else Color(0xFF00E676),
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Control buttons
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    .clickable { if (!isActive) onReset() else Toast.makeText(context, "Finish session first", Toast.LENGTH_SHORT).show() },
                contentAlignment = Alignment.Center,
            ) { Text("⟳", style = MaterialTheme.typography.titleLarge, color = TextPrimary) }
            Spacer(Modifier.width(28.dp))
            Box(
                modifier = Modifier.size(88.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(config.accentColor, config.accentColor.copy(alpha = 0.6f))))
                    .border(1.dp, config.accentColor.copy(alpha = 0.6f), CircleShape)
                    .clickable { onStart() },
                contentAlignment = Alignment.Center,
            ) { Text(if (uiState.state == FocusState.Active) "⏸" else "▶", style = MaterialTheme.typography.headlineMedium, color = Color.White) }
            Spacer(Modifier.width(28.dp))
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape)
                    .background(Danger.copy(alpha = 0.1f))
                    .border(1.dp, Danger.copy(alpha = 0.3f), CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center,
            ) { Text("■", style = MaterialTheme.typography.titleMedium, color = Danger) }
        }

        // Stats card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                FocusStatPill("Today's Focus", formatFocusMinutes(uiState.todayFocusMinutes), AccentPrimary)
                Box(Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.08f)).align(Alignment.CenterVertically))
                FocusStatPill("Sessions", "${uiState.todaySessionCount}", Success)
                Box(Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.08f)).align(Alignment.CenterVertically))
                FocusStatPill("Success", "${uiState.todaySuccessRate}%", if (uiState.todaySuccessRate >= 70) Success else Danger)
            }
        }

        // Chain Rules
        AppChainRulesSection(rules = uiState.chainRules, onAddRule = onAddRule, onDeleteRule = onDeleteRule)

        // Lo-Fi Ambient
        var isMusicPlaying by remember { mutableStateOf(false) }
        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = GlassCard,
            border = BorderStroke(1.dp, GlassCardBorder),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)))),
                        contentAlignment = Alignment.Center,
                    ) { Text(if (isMusicPlaying) "🎵" else "🌲", style = MaterialTheme.typography.titleLarge) }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Lo-Fi Forest", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            if (isMusicPlaying) "Now playing…" else "Ambient focus music",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isMusicPlaying) AccentSecondary else TextSecondary,
                        )
                    }
                }
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                        .background(if (isMusicPlaying) AccentSecondary.copy(0.15f) else Color.White.copy(0.05f))
                        .border(1.dp, if (isMusicPlaying) AccentSecondary.copy(0.4f) else Color.White.copy(0.1f), CircleShape)
                        .clickable {
                            isMusicPlaying = !isMusicPlaying
                            Toast.makeText(context, if (isMusicPlaying) "🎵 Playing Lo-Fi Forest…" else "⏸ Paused", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center,
                ) { Text(if (isMusicPlaying) "⏸" else "▶", style = MaterialTheme.typography.bodyLarge, color = if (isMusicPlaying) AccentSecondary else TextPrimary) }
            }
        }

        // Session history
        if (uiState.recentSessions.isNotEmpty()) {
            SessionHistorySection(sessions = uiState.recentSessions.take(7))
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Session History Section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SessionHistorySection(
    sessions: List<FocusSessionItem>,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Recent Sessions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            sessions.forEachIndexed { index, session ->
                if (index > 0) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                                .background(if (session.wasCompleted) Success.copy(alpha = 0.15f) else Danger.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(if (session.wasCompleted) "✓" else "✕", style = MaterialTheme.typography.labelLarge, color = if (session.wasCompleted) Success else Danger, fontWeight = FontWeight.Bold)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(session.mode, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("${session.dateLabel} · ${session.timeLabel}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(formatFocusMinutes(session.actualDurationMin), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        if (session.pointsEarned > 0) {
                            Text("+${session.pointsEarned} FP", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFB84D), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Completed Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FocusCompletedScreen(
    uiState: FocusUiState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().padding(LumetrixTokens.ScreenPadding), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Icon(Icons.Outlined.CheckCircle, null, tint = Success, modifier = Modifier.size(72.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Session Complete!", style = MaterialTheme.typography.headlineLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(
                    "You focused for ${uiState.selectedDurationMinutes} minutes\nin ${uiState.selectedMode} mode.",
                    style = MaterialTheme.typography.bodyLarge, color = TextSecondary, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
            }
            if (uiState.pointsJustEarned > 0) {
                GlassCard(modifier = Modifier.fillMaxWidth(0.75f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("⚡ +${uiState.pointsJustEarned} Focus Points", style = MaterialTheme.typography.headlineSmall, color = AccentPrimary, fontWeight = FontWeight.Bold)
                        Text("Balance: ${uiState.focusPointsBalance} pts", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
            }
            if (uiState.todayFocusMinutes > 0) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🔥", style = MaterialTheme.typography.bodyMedium)
                    Text("${formatFocusMinutes(uiState.todayFocusMinutes)} focused today", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
            PremiumPillButton(text = "Continue", onClick = onDismiss, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// App Chain Rules
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AppChainRulesSection(
    rules: List<AppChainRule>,
    onAddRule: () -> Unit,
    onDeleteRule: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Link, null, tint = AccentSecondary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("App Chain Rules", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                IconButton(onClick = onAddRule, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Add, "Add Rule", tint = AccentPrimary)
                }
            }
            if (rules.isEmpty()) {
                Text(
                    "No rules yet. Chain apps together: e.g. use Duolingo 10 min before Instagram.",
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary,
                )
            } else {
                rules.forEach { rule -> ChainRuleItem(rule = rule, onDelete = { onDeleteRule(rule.id) }) }
            }
        }
    }
}

@Composable
private fun ChainRuleItem(rule: AppChainRule, onDelete: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("📖 ${rule.gateAppName} → 📱 ${rule.targetAppName}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Text("Use ${rule.gateAppName} for ${rule.gateDurationMin}m first", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Outlined.Delete, "Delete", tint = Danger, modifier = Modifier.size(16.dp))
            }
        }
        val progressColor = if (rule.isSatisfied) Success else AccentPrimary
        LinearProgressIndicator(
            progress = { rule.gateProgress },
            modifier = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape),
            color = progressColor, trackColor = progressColor.copy(alpha = 0.15f),
        )
        if (rule.isSatisfied) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CheckCircle, null, tint = Success, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("Rule satisfied today!", style = MaterialTheme.typography.labelSmall, color = Success)
            }
        } else {
            Text("${rule.remainingMin}m more in ${rule.gateAppName} needed", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared Composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FocusStatPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Utilities
// ─────────────────────────────────────────────────────────────────────────────

private fun formatFocusMinutes(totalMinutes: Int): String {
    if (totalMinutes <= 0) return "0m"
    val hrs = totalMinutes / 60
    val mins = totalMinutes % 60
    return when {
        hrs > 0 && mins > 0 -> "${hrs}h ${mins}m"
        hrs > 0 -> "${hrs}h"
        else -> "${mins}m"
    }
}
