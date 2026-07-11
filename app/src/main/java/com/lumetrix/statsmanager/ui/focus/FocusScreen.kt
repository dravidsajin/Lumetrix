package com.lumetrix.statsmanager.ui.focus

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumetrix.statsmanager.domain.model.AppChainRule
import com.lumetrix.statsmanager.domain.model.FocusState
import com.lumetrix.statsmanager.ui.components.FocusModeCard
import com.lumetrix.statsmanager.ui.components.GlassCard
import com.lumetrix.statsmanager.ui.components.PremiumPillButton
import com.lumetrix.statsmanager.ui.theme.AccentGradientEnd
import com.lumetrix.statsmanager.ui.theme.AccentGradientStart
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.Danger
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.OrbGlow
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary
import com.lumetrix.statsmanager.ui.theme.Warning

private val focusModes = listOf("Deep Work", "Study", "Reading", "Workout")
private val focusDurations = listOf(15, 25, 50)

@Composable
fun FocusScreen(
    modifier: Modifier = Modifier,
    viewModel: FocusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState.state) {
        FocusState.Setup -> FocusSetupScreen(
            modifier = modifier,
            uiState = uiState,
            onSelectMode = { viewModel.selectMode(it) },
            onSelectDuration = { viewModel.selectDuration(it) },
            onStartSession = { viewModel.startSession() },
            onDeleteRule = { viewModel.deleteChainRule(it) },
            onAddRule = { viewModel.setAddRuleDialogVisible(true) },
        )
        FocusState.Active -> ImmersiveFocusSession(
            mode = uiState.selectedMode,
            timer = uiState.remainingTimeLabel,
            onExit = { viewModel.endSession() },
            modifier = modifier,
        )
        FocusState.Completed -> FocusCompletedScreen(
            modifier = modifier,
            uiState = uiState,
            onDismiss = { viewModel.dismissCompleted() }
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
                    targetAppName = targetApp.appName
                )
            }
        )
    }
}

@Composable
private fun FocusSetupScreen(
    uiState: com.lumetrix.statsmanager.domain.model.FocusUiState,
    onSelectMode: (String) -> Unit,
    onSelectDuration: (Int) -> Unit,
    onStartSession: () -> Unit,
    onDeleteRule: (Long) -> Unit,
    onAddRule: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LumetrixTokens.ScreenPadding),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Focus",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            modifier = Modifier.fillMaxWidth(),
        )
        // Feature 5: Focus Points balance in header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Enter a calm, distraction-free state",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = "⚡", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${uiState.focusPointsBalance} pts",
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(LumetrixTokens.CardSpacing))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing),
        ) {
            FocusTimerDisplay(timer = uiState.remainingTimeLabel)
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Duration",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    focusDurations.forEach { duration ->
                        val isSelected = uiState.selectedDurationMinutes == duration
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AccentPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { onSelectDuration(duration) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${duration}m",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) AccentPrimary else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "Focus Modes",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            modifier = Modifier.fillMaxWidth(),
        )

        focusModes.forEach { mode ->
            FocusModeCard(
                title = mode,
                selected = uiState.selectedMode == mode,
                onClick = { onSelectMode(mode) },
            )
        }
        
        Text(
            text = "Blocking ${uiState.distractingAppsCount} Distracting Apps",
            style = MaterialTheme.typography.bodyMedium,
            color = Warning,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Feature 6: App Chain Rules
        AppChainRulesSection(
            rules = uiState.chainRules,
            onAddRule = onAddRule,
            onDeleteRule = onDeleteRule,
        )

        PremiumPillButton(
            text = "Start Focus Session",
            onClick = onStartSession,
            modifier = Modifier.padding(top = 8.dp),
        )

        Spacer(modifier = Modifier.height(100.dp))
        } // Close inner scroll column
    } // Close outer column
}

@Composable
private fun FocusTimerDisplay(
    timer: String,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "focusTimer")
    val breathe by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathe",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(220.dp * breathe)) {
            val radius = size.minDimension / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrbGlow, Color.Transparent),
                    radius = radius,
                ),
                radius = radius,
            )
            drawCircle(
                color = AccentPrimary.copy(alpha = 0.2f),
                radius = radius * 0.78f,
                style = Stroke(width = 2.dp.toPx()),
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(AccentGradientStart, AccentGradientEnd, AccentGradientStart)),
                startAngle = -90f,
                sweepAngle = 300f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        Text(
            text = timer,
            style = MaterialTheme.typography.displayLarge,
            color = TextPrimary,
        )
    }
}

@Composable
private fun ImmersiveFocusSession(
    mode: String,
    timer: String,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = mode,
                style = MaterialTheme.typography.titleLarge,
                color = TextSecondary,
            )
            FocusTimerDisplay(timer = timer)
            Text(
                text = "Stay present. You're in focus.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
            )
            PremiumPillButton(
                text = "Give Up",
                onClick = onExit,
                modifier = Modifier
                    .padding(horizontal = LumetrixTokens.ScreenPadding)
                    .padding(top = 24.dp),
            )
        }
    }
}

@Composable
private fun FocusCompletedScreen(
    uiState: com.lumetrix.statsmanager.domain.model.FocusUiState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize().padding(LumetrixTokens.ScreenPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = Success,
                modifier = Modifier.size(64.dp),
            )
            Text(
                text = "Session Complete!",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
            )
            Text(
                text = "You focused for ${uiState.selectedDurationMinutes} minutes in ${uiState.selectedMode} mode.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            if (uiState.pointsJustEarned > 0) {
                GlassCard(modifier = Modifier.fillMaxWidth(0.7f)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "⚡ +${uiState.pointsJustEarned} Focus Points",
                            style = MaterialTheme.typography.headlineSmall,
                            color = AccentPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Balance: ${uiState.focusPointsBalance} pts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                }
            }
            PremiumPillButton(
                text = "Continue",
                onClick = onDismiss,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

/**
 * Feature 6: App Chain Rules section in Focus Setup.
 * Shows current rules with delete button, and a placeholder add button.
 */
@Composable
private fun AppChainRulesSection(
    rules: List<AppChainRule>,
    onAddRule: () -> Unit,
    onDeleteRule: (Long) -> Unit,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Link,
                        contentDescription = null,
                        tint = AccentSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "App Chain Rules",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                }
                IconButton(onClick = onAddRule, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add Rule",
                        tint = AccentPrimary,
                    )
                }
            }
            if (rules.isEmpty()) {
                Text(
                    text = "No rules yet. Chain apps together: e.g. use Duolingo 10 min before Instagram.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            } else {
                rules.forEach { rule ->
                    ChainRuleItem(rule = rule, onDelete = { onDeleteRule(rule.id) })
                }
            }
        }
    }
}

@Composable
private fun ChainRuleItem(rule: AppChainRule, onDelete: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "📖 ${rule.gateAppName} → 📱 ${rule.targetAppName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                )
                Text(
                    text = "Use ${rule.gateAppName} for ${rule.gateDurationMin}m first",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = Danger,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        val progressColor = if (rule.isSatisfied) Success else AccentPrimary
        LinearProgressIndicator(
            progress = { rule.gateProgress },
            modifier = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape),
            color = progressColor,
            trackColor = progressColor.copy(alpha = 0.15f),
        )
        if (rule.isSatisfied) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Success,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Rule satisfied today!",
                    style = MaterialTheme.typography.labelSmall,
                    color = Success,
                )
            }
        } else {
            Text(
                text = "${rule.remainingMin}m more in ${rule.gateAppName} needed",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
    }
}
