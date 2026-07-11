package com.lumetrix.statsmanager.ui.wellness

import android.widget.Toast
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.GlassCardBorder
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary
import kotlinx.coroutines.delay

// ── Data ──

private data class WellnessActivity(
    val id: String,
    val emoji: String,
    val name: String,
    val subtitle: String,
    val durationSec: Int,
    val color: Color,
    val type: String // "breathing", "timer", "counter", "checklist"
)

// ── Main Screen ──

@Composable
fun WellnessScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeActivity by remember { mutableStateOf<WellnessActivity?>(null) }
    var waterCount by remember { mutableIntStateOf(3) }

    val activities = remember {
        listOf(
            WellnessActivity("breath", "🧘", "Breathing", "2 min", 120, AccentPrimary, "breathing"),
            WellnessActivity("meditate", "🌳", "Meditation", "10 min", 600, AccentSecondary, "timer"),
            WellnessActivity("stretch", "🤸", "Stretch", "5 min", 300, Color(0xFFE040FB), "timer"),
            WellnessActivity("eye", "👁️", "Eye Exercise", "1 min", 60, Color(0xFF26C6DA), "timer"),
            WellnessActivity("water", "💧", "Water", "$waterCount/8 glasses", 0, Color(0xFF42A5F5), "counter"),
            WellnessActivity("posture", "🧍", "Posture", "Check", 0, Color(0xFFFFB74D), "checklist"),
            WellnessActivity("walk", "🚶", "Walking", "15 min", 900, Success, "timer"),
            WellnessActivity("mindful", "🧠", "Mindfulness", "5 min", 300, Color(0xFFFF7043), "timer")
        )
    }

    if (activeActivity != null) {
        when (activeActivity!!.type) {
            "breathing" -> BreathingDetailScreen(activity = activeActivity!!, onBack = { activeActivity = null })
            "counter" -> { waterCount = (waterCount + 1).coerceAtMost(8); Toast.makeText(context, "💧 $waterCount/8 glasses", Toast.LENGTH_SHORT).show(); activeActivity = null }
            "checklist" -> PostureCheckDialog(onDismiss = { activeActivity = null })
            else -> TimerDetailScreen(activity = activeActivity!!, onBack = { activeActivity = null })
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LumetrixTokens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Wellness Hub", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Daily Wellness", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            IconButton(onClick = { Toast.makeText(context, "Customize hub", Toast.LENGTH_SHORT).show() }) { Icon(Icons.Outlined.Settings, "Settings", tint = TextPrimary) }
        }

        // 2×4 Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(activities) { activity ->
                val subtitle = if (activity.type == "counter") "$waterCount/8 glasses" else activity.subtitle
                ActivityGridCard(
                    emoji = activity.emoji,
                    name = activity.name,
                    subtitle = subtitle,
                    color = activity.color,
                    onClick = { activeActivity = activity }
                )
            }
        }

        // Customize Hub button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(AccentPrimary.copy(alpha = 0.1f), AccentSecondary.copy(alpha = 0.1f))))
                .border(1.dp, AccentPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .clickable { Toast.makeText(context, "Customize your wellness hub", Toast.LENGTH_SHORT).show() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("+ Customize Hub", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = AccentPrimary)
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun ActivityGridCard(emoji: String, name: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.1f).clip(RoundedCornerShape(16.dp)).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = com.lumetrix.statsmanager.ui.theme.GlassCard,
        border = BorderStroke(1.dp, GlassCardBorder)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Text(emoji, style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary, textAlign = TextAlign.Center)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary, textAlign = TextAlign.Center)
        }
    }
}

// ── Breathing Detail Screen ──

@Composable
private fun BreathingDetailScreen(activity: WellnessActivity, onBack: () -> Unit) {
    var isRunning by remember { mutableStateOf(true) }
    var remainSec by remember { mutableIntStateOf(activity.durationSec) }

    LaunchedEffect(isRunning) {
        while (isRunning && remainSec > 0) {
            delay(1000L)
            remainSec--
        }
    }

    // Breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "breathAnim")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse), label = "scale"
    )
    val isInhale = breathScale > 0.8f

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = LumetrixTokens.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary) }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Breathing", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Animated breathing circle
        Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
            // Outer glow rings
            Canvas(modifier = Modifier.size(220.dp).scale(breathScale)) {
                drawCircle(Brush.radialGradient(listOf(AccentPrimary.copy(alpha = 0.08f), Color.Transparent)), radius = size.minDimension / 2)
            }
            Canvas(modifier = Modifier.size(180.dp).scale(breathScale)) {
                drawCircle(Brush.radialGradient(listOf(AccentPrimary.copy(alpha = 0.15f), Color.Transparent)), radius = size.minDimension / 2)
            }
            // Main circle
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(breathScale)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(AccentPrimary.copy(alpha = 0.4f), AccentSecondary.copy(alpha = 0.2f)))),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isInhale) "Inhale" else "Exhale", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }

        // Timer
        Text("${remainSec / 60}:${String.format("%02d", remainSec % 60)}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = TextPrimary)

        // Controls
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // Reset
            IconButton(onClick = { remainSec = activity.durationSec; isRunning = false }) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.06f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.RestartAlt, "Reset", tint = TextPrimary)
                }
            }
            // Play/Pause
            IconButton(onClick = { isRunning = !isRunning }) {
                Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Brush.linearGradient(listOf(AccentPrimary, AccentSecondary))), contentAlignment = Alignment.Center) {
                    Icon(if (isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, "Toggle", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
            // Close
            IconButton(onClick = onBack) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.06f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, "Stop", tint = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// ── Timer Detail Screen ──

@Composable
private fun TimerDetailScreen(activity: WellnessActivity, onBack: () -> Unit) {
    var isRunning by remember { mutableStateOf(false) }
    var remainSec by remember { mutableIntStateOf(activity.durationSec) }

    LaunchedEffect(isRunning) {
        while (isRunning && remainSec > 0) {
            delay(1000L)
            remainSec--
        }
    }

    val progress = 1f - (remainSec.toFloat() / activity.durationSec.toFloat())

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = LumetrixTokens.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary) }
            Spacer(modifier = Modifier.width(8.dp))
            Text(activity.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Timer ring
        Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(200.dp)) {
                drawArc(Color.White.copy(alpha = 0.06f), 0f, 360f, false, style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
                drawArc(brush = Brush.sweepGradient(listOf(activity.color, activity.color.copy(alpha = 0.4f), activity.color)),
                    startAngle = -90f, sweepAngle = progress * 360f, useCenter = false, style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(activity.emoji, style = MaterialTheme.typography.headlineMedium)
                Text("${remainSec / 60}:${String.format("%02d", remainSec % 60)}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }

        // Controls
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            IconButton(onClick = { remainSec = activity.durationSec; isRunning = false }) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.06f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.RestartAlt, "Reset", tint = TextPrimary)
                }
            }
            IconButton(onClick = { isRunning = !isRunning }) {
                Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Brush.linearGradient(listOf(activity.color, activity.color.copy(alpha = 0.7f)))), contentAlignment = Alignment.Center) {
                    Icon(if (isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, "Toggle", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
            IconButton(onClick = onBack) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.06f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, "Stop", tint = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// ── Posture Check Dialog ──

@Composable
private fun PostureCheckDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val checks = remember { mutableStateOf(listOf(false, false, false, false)) }
    val labels = listOf("Feet flat on floor", "Back straight", "Shoulders relaxed", "Screen at eye level")

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = LumetrixTokens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary) }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Posture Check", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Check your posture", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("Tap each item when you've adjusted", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                labels.forEachIndexed { idx, label ->
                    val checked = checks.value[idx]
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(if (checked) Success.copy(alpha = 0.08f) else Color.Transparent)
                            .border(1.dp, if (checked) Success.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .clickable { checks.value = checks.value.toMutableList().also { it[idx] = !it[idx] } }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(if (checked) Success else Color.Transparent).border(2.dp, if (checked) Success else Color.White.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                            if (checked) Icon(Icons.Outlined.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Text(label, style = MaterialTheme.typography.bodyMedium, color = if (checked) Success else TextPrimary, fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal)
                    }
                }

                if (checks.value.all { it }) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("✅ Great posture! Keep it up!", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Success, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
