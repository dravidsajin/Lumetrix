package com.lumetrix.statsmanager.ui.focus

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.lumetrix.statsmanager.ui.components.FocusModeCard
import com.lumetrix.statsmanager.ui.components.PremiumPillButton
import com.lumetrix.statsmanager.ui.theme.AccentGradientEnd
import com.lumetrix.statsmanager.ui.theme.AccentGradientStart
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.OrbGlow
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary

private val focusModes = listOf("Deep Work", "Study", "Reading", "Workout")

@Composable
fun FocusScreen(modifier: Modifier = Modifier) {
    var selectedMode by remember { mutableIntStateOf(0) }
    var sessionActive by remember { mutableStateOf(false) }

    if (sessionActive) {
        ImmersiveFocusSession(
            mode = focusModes[selectedMode],
            timer = "25:00",
            onExit = { sessionActive = false },
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = LumetrixTokens.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing),
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

        FocusTimerDisplay(timer = "25:00")

        Text(
            text = "Focus Modes",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            modifier = Modifier.fillMaxWidth(),
        )

        focusModes.forEachIndexed { index, mode ->
            FocusModeCard(
                title = mode,
                selected = selectedMode == index,
                onClick = { selectedMode = index },
            )
        }

        PremiumPillButton(
            text = "Start Focus Session",
            onClick = { sessionActive = true },
            modifier = Modifier.padding(top = 8.dp),
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
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
                text = "End Session",
                onClick = onExit,
                modifier = Modifier
                    .padding(horizontal = LumetrixTokens.ScreenPadding)
                    .padding(top = 24.dp),
            )
        }
    }
}
