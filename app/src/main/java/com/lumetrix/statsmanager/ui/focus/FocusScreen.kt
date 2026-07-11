package com.lumetrix.statsmanager.ui.focus

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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumetrix.statsmanager.core.time.DateUtils
import com.lumetrix.statsmanager.domain.model.AppChainRule
import com.lumetrix.statsmanager.domain.model.FocusState
import com.lumetrix.statsmanager.ui.components.GlassCard
import com.lumetrix.statsmanager.ui.components.PremiumPillButton
import com.lumetrix.statsmanager.ui.components.ScreenHeader
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.Danger
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary
import com.lumetrix.statsmanager.ui.theme.GlassCardBorder

private data class FocusModeConfig(
    val name: String,
    val icon: String,
    val accentColor: Color,
    val bgColor: Color,
    val goalText: String,
    val goalProgress: Float
)

@Composable
fun FocusScreen(
    modifier: Modifier = Modifier,
    viewModel: FocusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Focus modes list definitions
    val modes = remember {
        listOf(
            FocusModeConfig("Deep Work", "🌙", Color(0xFF8126F2), Color(0xFF1E1A3C), "2h 30m / 4h", 0.625f),
            FocusModeConfig("Study", "🎓", Color(0xFF2196F3), Color(0xFF152A4A), "1h 15m / 2h", 0.625f),
            FocusModeConfig("Reading", "📖", Color(0xFF00E676), Color(0xFF1A3B2A), "45m / 1h", 0.75f),
            FocusModeConfig("Workout", "💪", Color(0xFFFFB74D), Color(0xFF3E2D1A), "30m / 1h", 0.5f),
            FocusModeConfig("Sleep", "🌙", Color(0xFF9C27B0), Color(0xFF1D1B44), "7h 15m / 8h", 0.9f),
            FocusModeConfig("Driving", "🚗", Color(0xFF00B0FF), Color(0xFF143547), "15m / 30m", 0.5f),
            FocusModeConfig("Meeting", "👥", Color(0xFFD7CCC8), Color(0xFF382A24), "1h 00m / 1h 30m", 0.66f),
            FocusModeConfig("Gaming", "🎮", Color(0xFFFF5252), Color(0xFF3E1C27), "1h 30m / 2h", 0.75f)
        )
    }

    var selectedModeDetail by remember { mutableStateOf<FocusModeConfig?>(null) }

    // Sync selected mode with ViewModel when navigation occurs
    LaunchedEffect(selectedModeDetail) {
        selectedModeDetail?.let {
            viewModel.selectMode(it.name)
        }
    }

    // Auto-return to grid state when completed session is dismissed
    LaunchedEffect(uiState.state) {
        if (uiState.state == FocusState.Setup) {
            selectedModeDetail = null
        }
    }

    when (uiState.state) {
        FocusState.Setup -> {
            if (selectedModeDetail == null) {
                // Screen 1: Focus grid selector
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = LumetrixTokens.ScreenPadding),
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    ScreenHeader(
                        title = "Focus",
                        subtitle = "Enter a calm, distraction-free state",
                        actionContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFFFB84D).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFFFFB84D).copy(alpha = 0.3f), RoundedCornerShape(50))
                                    .padding(vertical = 4.dp, horizontal = 10.dp)
                            ) {
                                Text(text = "⚡", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = "${uiState.focusPointsBalance} FP",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFFFB84D),
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    )

                    FocusGridSelector(
                        uiState = uiState,
                        modes = modes,
                        onModeSelected = { selectedModeDetail = it },
                        onAddCustomFocus = { viewModel.setAddRuleDialogVisible(true) },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Screen 2: Focus timer configurator & Stats Dashboard (Unified setup & active state)
                FocusTimerDashboard(
                    config = selectedModeDetail!!,
                    uiState = uiState,
                    isActive = false,
                    onBack = { selectedModeDetail = null },
                    onStart = { viewModel.startSession() },
                    onReset = { viewModel.selectDuration(uiState.selectedDurationMinutes) },
                    onOpenSettings = { viewModel.setAddRuleDialogVisible(true) },
                    modifier = modifier
                )
            }
        }
        FocusState.Active -> {
            val activeConfig = modes.firstOrNull { it.name == uiState.selectedMode } ?: modes.first()
            // Screen 2 active countdown state (uses the exact same layout dashboard but animated countdown!)
            FocusTimerDashboard(
                config = activeConfig,
                uiState = uiState,
                isActive = true,
                onBack = { viewModel.endSession() },
                onStart = { viewModel.endSession() }, // Clicking pause stops/ends session
                onReset = { viewModel.selectDuration(uiState.selectedDurationMinutes) },
                onOpenSettings = { viewModel.setAddRuleDialogVisible(true) },
                modifier = modifier
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
                    targetAppName = targetApp.appName
                )
            }
        )
    }
}

@Composable
private fun FocusGridSelector(
    uiState: com.lumetrix.statsmanager.domain.model.FocusUiState,
    modes: List<FocusModeConfig>,
    onModeSelected: (FocusModeConfig) -> Unit,
    onAddCustomFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)
    ) {
        Text(
            text = "Choose a Focus Mode",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
        )
        
        // 2-column layout grid using Rows
        for (i in modes.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FocusModeGridItem(
                    config = modes[i],
                    onClick = { onModeSelected(modes[i]) },
                    modifier = Modifier.weight(1f)
                )
                if (i + 1 < modes.size) {
                    FocusModeGridItem(
                        config = modes[i + 1],
                        onClick = { onModeSelected(modes[i + 1]) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // FAB style Custom Focus
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, GlassCardBorder.copy(alpha = 0.15f), CircleShape)
                    .clickable { onAddCustomFocus() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "Custom Focus",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
        }
        
        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
private fun FocusModeGridItem(
    config: FocusModeConfig,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(115.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(config.bgColor)
            .border(1.dp, config.accentColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(config.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = config.icon,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            Text(
                text = config.name,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FocusTimerDashboard(
    config: FocusModeConfig,
    uiState: com.lumetrix.statsmanager.domain.model.FocusUiState,
    isActive: Boolean,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onReset: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Dynamic calculations from database focus sessions history
    val todaySessions = remember(uiState.recentSessions) {
        uiState.recentSessions.filter { it.dateLabel == "Today" }
    }
    
    val todayFocusMin = todaySessions.sumOf { it.actualDurationMin }
    val todayFocusLabel = remember(todayFocusMin) {
        val hrs = todayFocusMin / 60
        val mins = todayFocusMin % 60
        if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
    }
    
    val completedSessionsCount = todaySessions.count { it.wasCompleted }
    val successRate = remember(todaySessions, completedSessionsCount) {
        if (todaySessions.isNotEmpty()) {
            (completedSessionsCount * 100) / todaySessions.size
        } else 0
    }

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

        // Custom Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            
            Text(
                text = "Focus",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = "Shield Settings",
                    tint = TextPrimary
                )
            }
        }

        // Top Pills Tabs Switcher Row
        val tabs = listOf("Focus", "Pomodoro", "Zen Mode", "Custom")
        var activeTab by remember { mutableStateOf("Focus") }
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { tab ->
                val isSelected = activeTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) AccentSecondary.copy(alpha = 0.2f) else Color.Transparent)
                        .border(1.dp, if (isSelected) AccentSecondary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(50))
                        .clickable { 
                            activeTab = tab 
                            if (tab != "Focus") {
                                Toast.makeText(context, "$tab coming soon!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) AccentSecondary else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Circular countdown visualizer
        val totalTime = uiState.selectedDurationMinutes * 60 * 1000f
        val progress = if (isActive) (uiState.remainingTimeMillis / totalTime).coerceIn(0f, 1f) else 1f
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
                val radius = size.minDimension / 2f
                val strokeWidth = 8.dp.toPx()
                
                // Track backing circle
                drawCircle(
                    color = Color.White.copy(alpha = 0.04f),
                    radius = radius - strokeWidth,
                    style = Stroke(width = strokeWidth)
                )
                
                // Sweeping visual gradient arc (Purple to Cyan)
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            Color(0xFF8126F2),
                            Color(0xFF00E5FF),
                            Color(0xFF8126F2)
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = config.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = uiState.remainingTimeLabel,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                )
                Text(
                    text = if (isActive) "You're doing great! 💪" else "Focus on your goal 🌿",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) Color(0xFF00E5FF) else Color(0xFF00E676),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Action Controls row
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    .clickable { onReset() },
                contentAlignment = Alignment.Center
            ) {
                Text("⟳", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            }

            Spacer(modifier = Modifier.width(28.dp))

            // Play/Pause Action Button (large brand gradient circle)
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF8126F2), Color(0xFF5E35B1))
                        )
                    )
                    .border(1.dp, Color(0xFF8126F2).copy(alpha = 0.6f), CircleShape)
                    .clickable { onStart() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isActive) "⏸" else "▶",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(28.dp))

            // Stop button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text("■", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            }
        }

        // Allowed Apps section (setup only)
        if (!isActive) {
            Surface(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = com.lumetrix.statsmanager.ui.theme.GlassCard,
                border = BorderStroke(1.dp, GlassCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Allowed Apps", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("These apps remain accessible during focus", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val allowedApps = listOf("\uD83D\uDCDD" to "Notion", "\uD83C\uDFB5" to "Spotify", "\u2709\uFE0F" to "Telegram")
                        allowedApps.forEach { (icon, name) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.06f)), contentAlignment = Alignment.Center) {
                                    Text(icon, style = MaterialTheme.typography.titleMedium)
                                }
                                Text(name, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(AccentPrimary.copy(alpha = 0.1f)).border(1.dp, AccentPrimary.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Add, null, tint = AccentPrimary, modifier = Modifier.size(20.dp))
                            }
                            Text("+2", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }
                }
            }
        }

        // Stats card (Today's Focus, Sessions, Completed)
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1.2f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Today's Focus",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = todayFocusLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(Color.White.copy(alpha = 0.08f))
                        .align(Alignment.CenterVertically)
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Sessions",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${todaySessions.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(Color.White.copy(alpha = 0.08f))
                        .align(Alignment.CenterVertically)
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "$successRate%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }

        // Lo-Fi Ambient Music Card
        var isMusicPlaying by remember { mutableStateOf(false) }
        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = com.lumetrix.statsmanager.ui.theme.GlassCard,
            border = BorderStroke(1.dp, GlassCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Artwork slot
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF00796B), Color(0xFF004D40)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌲", style = MaterialTheme.typography.titleLarge)
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Lo-Fi Forest",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Ambient",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
                
                // Play button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                        .clickable {
                            isMusicPlaying = !isMusicPlaying
                            if (isMusicPlaying) {
                                Toast.makeText(context, "Playing Lo-Fi Forest ambient tracks...", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Ambient music paused.", Toast.LENGTH_SHORT).show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isMusicPlaying) "⏸" else "▶",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun FocusCompletedScreen(
    uiState: com.lumetrix.statsmanager.domain.model.FocusUiState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(LumetrixTokens.ScreenPadding),
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
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(CircleShape),
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
