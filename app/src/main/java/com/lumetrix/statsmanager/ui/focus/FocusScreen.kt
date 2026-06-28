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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumetrix.statsmanager.domain.model.FocusState
import com.lumetrix.statsmanager.ui.components.FocusModeCard
import com.lumetrix.statsmanager.ui.components.GlassCard
import com.lumetrix.statsmanager.ui.components.PremiumPillButton
import com.lumetrix.statsmanager.ui.theme.AccentGradientEnd
import com.lumetrix.statsmanager.ui.theme.AccentGradientStart
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.OrbGlow
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
            onStartSession = { viewModel.startSession() }
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
            onDismiss = { viewModel.endSession() }
        )
    }
}

@Composable
private fun FocusSetupScreen(
    uiState: com.lumetrix.statsmanager.domain.model.FocusUiState,
    onSelectMode: (String) -> Unit,
    onSelectDuration: (Int) -> Unit,
    onStartSession: () -> Unit,
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
        Text(
            text = "Enter a calm, distraction-free state",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth(),
        )

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
            Text(
                text = "Session Complete!",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
            )
            Text(
                text = "You successfully focused for ${uiState.selectedDurationMinutes} minutes in ${uiState.selectedMode} mode.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            PremiumPillButton(
                text = "Continue",
                onClick = onDismiss,
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }
}
