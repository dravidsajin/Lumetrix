package com.lumetrix.statsmanager.ui.settings

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lumetrix.statsmanager.ui.components.ScreenHeader
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.Warning
import com.lumetrix.statsmanager.ui.theme.Danger
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary
import com.lumetrix.statsmanager.ui.theme.GlassCardBorder

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateToOverlay: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var activeSettingSubScreen by remember { mutableStateOf<String?>(null) }

    // Core Settings States (Persistent during app session lifetime)
    var isAutoStartFocus by remember { mutableStateOf(false) }
    var isWeeklyReminder by remember { mutableStateOf(true) }
    var isAggressiveBlocking by remember { mutableStateOf(false) }
    var selectedHomeTab by remember { mutableStateOf("Pulse") }

    var selectedThemeColor by remember { mutableStateOf("Sunset Purple") }
    var cardOpacity by remember { mutableFloatStateOf(0.1f) }
    var isGlowEffectsEnabled by remember { mutableStateOf(true) }

    var isDailyDigestEnabled by remember { mutableStateOf(true) }
    var isBreathingRemindersEnabled by remember { mutableStateOf(true) }
    var isPostureAlertsEnabled by remember { mutableStateOf(false) }
    var isBedtimeWindDownEnabled by remember { mutableStateOf(true) }

    var defaultFocusDuration by remember { mutableStateOf("25 min") }
    var shortBreakDuration by remember { mutableStateOf("5 min") }
    var longBreakDuration by remember { mutableStateOf("15 min") }
    var isStrictFocusEnabled by remember { mutableStateOf(false) }

    var isLocalBackupEnabled by remember { mutableStateOf(true) }
    var isAnonymousDiagnosticsEnabled by remember { mutableStateOf(false) }
    var isRequireDeviceLock by remember { mutableStateOf(false) }

    var coachPersonality by remember { mutableStateOf("Encouraging") }
    var insightDeliveryFrequency by remember { mutableStateOf("Weekly") }
    var assistantTone by remember { mutableStateOf("Motivating") }

    // Show Detail Sub-screen if selected
    when (activeSettingSubScreen) {
        "general" -> {
            GeneralSettingsSubScreen(
                isAutoStartFocus = isAutoStartFocus,
                onAutoStartFocusChange = { isAutoStartFocus = it },
                isWeeklyReminder = isWeeklyReminder,
                onWeeklyReminderChange = { isWeeklyReminder = it },
                isAggressiveBlocking = isAggressiveBlocking,
                onAggressiveBlockingChange = { isAggressiveBlocking = it },
                selectedHomeTab = selectedHomeTab,
                onHomeTabChange = { selectedHomeTab = it },
                onBack = { activeSettingSubScreen = null }
            )
        }
        "theme" -> {
            ThemeSettingsSubScreen(
                selectedThemeColor = selectedThemeColor,
                onThemeColorChange = { selectedThemeColor = it },
                cardOpacity = cardOpacity,
                onCardOpacityChange = { cardOpacity = it },
                isGlowEffectsEnabled = isGlowEffectsEnabled,
                onGlowEffectsChange = { isGlowEffectsEnabled = it },
                onBack = { activeSettingSubScreen = null }
            )
        }
        "notifications" -> {
            NotificationsSettingsSubScreen(
                isDailyDigestEnabled = isDailyDigestEnabled,
                onDailyDigestChange = { isDailyDigestEnabled = it },
                isBreathingRemindersEnabled = isBreathingRemindersEnabled,
                onBreathingRemindersChange = { isBreathingRemindersEnabled = it },
                isPostureAlertsEnabled = isPostureAlertsEnabled,
                onPostureAlertsChange = { isPostureAlertsEnabled = it },
                isBedtimeWindDownEnabled = isBedtimeWindDownEnabled,
                onBedtimeWindDownChange = { isBedtimeWindDownEnabled = it },
                onBack = { activeSettingSubScreen = null }
            )
        }
        "focus" -> {
            FocusPreferencesSubScreen(
                defaultFocusDuration = defaultFocusDuration,
                onDefaultFocusDurationChange = { defaultFocusDuration = it },
                shortBreakDuration = shortBreakDuration,
                onShortBreakDurationChange = { shortBreakDuration = it },
                longBreakDuration = longBreakDuration,
                onLongBreakDurationChange = { longBreakDuration = it },
                isStrictFocusEnabled = isStrictFocusEnabled,
                onStrictFocusChange = { isStrictFocusEnabled = it },
                onBack = { activeSettingSubScreen = null }
            )
        }
        "privacy" -> {
            DataPrivacySubScreen(
                isLocalBackupEnabled = isLocalBackupEnabled,
                onLocalBackupChange = { isLocalBackupEnabled = it },
                isAnonymousDiagnosticsEnabled = isAnonymousDiagnosticsEnabled,
                onAnonymousDiagnosticsChange = { isAnonymousDiagnosticsEnabled = it },
                isRequireDeviceLock = isRequireDeviceLock,
                onRequireDeviceLockChange = { isRequireDeviceLock = it },
                onBack = { activeSettingSubScreen = null }
            )
        }
        "aicoach" -> {
            AICoachSubScreen(
                coachPersonality = coachPersonality,
                onCoachPersonalityChange = { coachPersonality = it },
                insightDeliveryFrequency = insightDeliveryFrequency,
                onInsightDeliveryChange = { insightDeliveryFrequency = it },
                assistantTone = assistantTone,
                onAssistantToneChange = { assistantTone = it },
                onBack = { activeSettingSubScreen = null }
            )
        }
        "about" -> {
            AboutSubScreen(
                onBack = { activeSettingSubScreen = null }
            )
        }
        else -> {
            // Main Settings Screen List
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = LumetrixTokens.ScreenPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                ScreenHeader(
                    title = "Settings",
                    subtitle = "Customize your Lumetrix experience"
                )

                // Category 1: Preferences
                SettingsCategorySection(title = "PREFERENCES") {
                    SettingsRow(
                        icon = Icons.Outlined.Settings,
                        title = "General",
                        onClick = { activeSettingSubScreen = "general" }
                    )
                    SettingsDivider()

                    SettingsRow(
                        icon = Icons.Outlined.Palette,
                        title = "Theme",
                        value = selectedThemeColor,
                        showPurpleDot = true,
                        onClick = { activeSettingSubScreen = "theme" }
                    )
                    SettingsDivider()

                    SettingsRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        onClick = { activeSettingSubScreen = "notifications" }
                    )
                }

                // Category 2: Wellbeing & Goals
                SettingsCategorySection(title = "WELLBEING & GOALS") {
                    SettingsRow(
                        icon = Icons.Outlined.Star,
                        title = "Goals",
                        onClick = { onNavigateToOverlay("goals") }
                    )
                    SettingsDivider()

                    SettingsRow(
                        icon = Icons.Outlined.Timer,
                        title = "Sleep Details",
                        onClick = { onNavigateToOverlay("sleep") }
                    )
                    SettingsDivider()

                    SettingsRow(
                        icon = Icons.Outlined.Favorite,
                        title = "Wellness Routine",
                        onClick = { onNavigateToOverlay("wellness") }
                    )
                }

                // Category 3: Focus & Security
                SettingsCategorySection(title = "FOCUS & SECURITY") {
                    SettingsRow(
                        icon = Icons.Outlined.Timer,
                        title = "Focus Preferences",
                        onClick = { activeSettingSubScreen = "focus" }
                    )
                    SettingsDivider()

                    SettingsRow(
                        icon = Icons.Outlined.Lock,
                        title = "Data & Privacy",
                        onClick = { activeSettingSubScreen = "privacy" }
                    )
                    SettingsDivider()

                    SettingsRow(
                        icon = Icons.Outlined.Person,
                        title = "AI Coach",
                        value = coachPersonality,
                        onClick = { activeSettingSubScreen = "aicoach" }
                    )
                    SettingsDivider()

                    SettingsRow(
                        icon = Icons.Outlined.Star,
                        title = "Weekly Report",
                        onClick = { onNavigateToOverlay("report") }
                    )
                }

                // Category 4: About
                SettingsCategorySection(title = "ABOUT") {
                    SettingsRow(
                        icon = Icons.Outlined.Info,
                        title = "About Lumetrix",
                        value = "v1.0.0",
                        onClick = { activeSettingSubScreen = "about" }
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

// ── PREMIUM CUSTOM SWITCH ──
@Composable
fun LumetrixSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 2.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "thumbOffset"
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) AccentPrimary else Color.White.copy(alpha = 0.08f),
        label = "trackColor"
    )
    val thumbColor = if (checked) Color.White else TextSecondary

    Box(
        modifier = modifier
            .size(width = 44.dp, height = 24.dp)
            .clip(CircleShape)
            .background(trackColor)
            .border(1.dp, if (checked) AccentPrimary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f), CircleShape)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(18.dp)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

// ── CUSTOM COMPOSE ROW WITH SWITCH ──
@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        LumetrixSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

// ── SUB-SCREENS ──

@Composable
private fun GeneralSettingsSubScreen(
    isAutoStartFocus: Boolean,
    onAutoStartFocusChange: (Boolean) -> Unit,
    isWeeklyReminder: Boolean,
    onWeeklyReminderChange: (Boolean) -> Unit,
    isAggressiveBlocking: Boolean,
    onAggressiveBlockingChange: (Boolean) -> Unit,
    selectedHomeTab: String,
    onHomeTabChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LumetrixTokens.ScreenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("General", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = com.lumetrix.statsmanager.ui.theme.GlassCard,
            border = BorderStroke(1.dp, GlassCardBorder)
        ) {
            Column {
                SwitchRow(
                    title = "Auto-Start Focus Mode",
                    subtitle = "Instantly open focus mode upon app launch",
                    checked = isAutoStartFocus,
                    onCheckedChange = onAutoStartFocusChange
                )
                SettingsDivider()
                SwitchRow(
                    title = "Weekly Report Reminders",
                    subtitle = "Get notified when Weekly AI Coach insights are compiled",
                    checked = isWeeklyReminder,
                    onCheckedChange = onWeeklyReminderChange
                )
                SettingsDivider()
                SwitchRow(
                    title = "Aggressive App Blocking",
                    subtitle = "Immediately lock restricted apps without warning prompts",
                    checked = isAggressiveBlocking,
                    onCheckedChange = onAggressiveBlockingChange
                )
            }
        }

        Text(
            text = "DEFAULT HOME TAB",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = TextSecondary.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf("Pulse", "Insights", "Focus", "Apps")
            tabs.forEach { tab ->
                val isSelected = selectedHomeTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) AccentSecondary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                        .border(1.dp, if (isSelected) AccentSecondary else Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .clickable { onHomeTabChange(tab) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeSettingsSubScreen(
    selectedThemeColor: String,
    onThemeColorChange: (String) -> Unit,
    cardOpacity: Float,
    onCardOpacityChange: (Float) -> Unit,
    isGlowEffectsEnabled: Boolean,
    onGlowEffectsChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LumetrixTokens.ScreenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Theme Customization", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Text(
            text = "ACCENT THEME COLOR",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = TextSecondary.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val themeOptions = listOf(
                "Sunset Purple" to AccentPrimary,
                "Neon Aurora" to Success,
                "Cyberpunk Red" to Danger,
                "Ocean Breeze" to Color(0xFF2196F3)
            )
            themeOptions.forEach { (name, color) ->
                val isSelected = selectedThemeColor == name
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onThemeColorChange(name) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(2.dp, if (isSelected) Color.White else Color.Transparent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = com.lumetrix.statsmanager.ui.theme.GlassCard,
            border = BorderStroke(1.dp, GlassCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Glassmorphic Opacity",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(cardOpacity * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentSecondary
                    )
                    Slider(
                        value = cardOpacity,
                        onValueChange = onCardOpacityChange,
                        valueRange = 0.05f..0.3f,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentSecondary,
                            activeTrackColor = AccentSecondary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = com.lumetrix.statsmanager.ui.theme.GlassCard,
            border = BorderStroke(1.dp, GlassCardBorder)
        ) {
            SwitchRow(
                title = "Ambient Glow Orbs",
                subtitle = "Enable organic floating gradients in background screen areas",
                checked = isGlowEffectsEnabled,
                onCheckedChange = onGlowEffectsChange
            )
        }
    }
}

@Composable
private fun NotificationsSettingsSubScreen(
    isDailyDigestEnabled: Boolean,
    onDailyDigestChange: (Boolean) -> Unit,
    isBreathingRemindersEnabled: Boolean,
    onBreathingRemindersChange: (Boolean) -> Unit,
    isPostureAlertsEnabled: Boolean,
    onPostureAlertsChange: (Boolean) -> Unit,
    isBedtimeWindDownEnabled: Boolean,
    onBedtimeWindDownChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LumetrixTokens.ScreenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Notifications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = com.lumetrix.statsmanager.ui.theme.GlassCard,
            border = BorderStroke(1.dp, GlassCardBorder)
        ) {
            Column {
                SwitchRow(
                    title = "Daily Digest Summary",
                    subtitle = "Get a summary of productivity and screen time at 9:00 PM",
                    checked = isDailyDigestEnabled,
                    onCheckedChange = onDailyDigestChange
                )
                SettingsDivider()
                SwitchRow(
                    title = "Breathing Break Prompts",
                    subtitle = "Encourage minor breathing exercises during long work slots",
                    checked = isBreathingRemindersEnabled,
                    onCheckedChange = onBreathingRemindersChange
                )
                SettingsDivider()
                SwitchRow(
                    title = "Posture Correct Reminders",
                    subtitle = "Remind you to check and straighten your posture every 45 mins",
                    checked = isPostureAlertsEnabled,
                    onCheckedChange = onPostureAlertsChange
                )
                SettingsDivider()
                SwitchRow(
                    title = "Bedtime Wind Down Alert",
                    subtitle = "Prompt to turn off device screens 1 hr prior to sleep bedtime",
                    checked = isBedtimeWindDownEnabled,
                    onCheckedChange = onBedtimeWindDownChange
                )
            }
        }
    }
}

@Composable
private fun FocusPreferencesSubScreen(
    defaultFocusDuration: String,
    onDefaultFocusDurationChange: (String) -> Unit,
    shortBreakDuration: String,
    onShortBreakDurationChange: (String) -> Unit,
    longBreakDuration: String,
    onLongBreakDurationChange: (String) -> Unit,
    isStrictFocusEnabled: Boolean,
    onStrictFocusChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LumetrixTokens.ScreenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Focus Preferences", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Text(
            text = "DEFAULT FOCUS TIME",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = TextSecondary.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val focusTimes = listOf("15 min", "25 min", "45 min", "60 min")
            focusTimes.forEach { time ->
                val isSelected = defaultFocusDuration == time
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) AccentSecondary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                        .border(1.dp, if (isSelected) AccentSecondary else Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .clickable { onDefaultFocusDurationChange(time) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = time,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Text(
            text = "SHORT BREAK",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = TextSecondary.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val breakTimes = listOf("5 min", "10 min", "15 min")
            breakTimes.forEach { time ->
                val isSelected = shortBreakDuration == time
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Success.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                        .border(1.dp, if (isSelected) Success else Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .clickable { onShortBreakDurationChange(time) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = time,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Text(
            text = "LONG BREAK",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = TextSecondary.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val longBreaks = listOf("15 min", "20 min", "30 min")
            longBreaks.forEach { time ->
                val isSelected = longBreakDuration == time
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) AccentPrimary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                        .border(1.dp, if (isSelected) AccentPrimary else Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .clickable { onLongBreakDurationChange(time) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = time,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = com.lumetrix.statsmanager.ui.theme.GlassCard,
            border = BorderStroke(1.dp, GlassCardBorder)
        ) {
            SwitchRow(
                title = "Strict Focus Mode",
                subtitle = "Hide pause and reset buttons to block focus escapism completely",
                checked = isStrictFocusEnabled,
                onCheckedChange = onStrictFocusChange
            )
        }
    }
}

@Composable
private fun DataPrivacySubScreen(
    isLocalBackupEnabled: Boolean,
    onLocalBackupChange: (Boolean) -> Unit,
    isAnonymousDiagnosticsEnabled: Boolean,
    onAnonymousDiagnosticsChange: (Boolean) -> Unit,
    isRequireDeviceLock: Boolean,
    onRequireDeviceLockChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LumetrixTokens.ScreenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Data & Privacy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = com.lumetrix.statsmanager.ui.theme.GlassCard,
            border = BorderStroke(1.dp, GlassCardBorder)
        ) {
            Column {
                SwitchRow(
                    title = "Local Encrypted Backups",
                    subtitle = "Keep automatic hourly backups of metrics stored in cache",
                    checked = isLocalBackupEnabled,
                    onCheckedChange = onLocalBackupChange
                )
                SettingsDivider()
                SwitchRow(
                    title = "Anonymous Telemetry",
                    subtitle = "Send anonymous error and crash reports to diagnostics server",
                    checked = isAnonymousDiagnosticsEnabled,
                    onCheckedChange = onAnonymousDiagnosticsChange
                )
                SettingsDivider()
                SwitchRow(
                    title = "Require Pin / Bio-lock",
                    subtitle = "Prompt device authentication before leaving strict focus mode",
                    checked = isRequireDeviceLock,
                    onCheckedChange = onRequireDeviceLockChange
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    Toast.makeText(context, "Tracking logs cleared completely", Toast.LENGTH_SHORT).show()
                },
            shape = RoundedCornerShape(16.dp),
            color = com.lumetrix.statsmanager.ui.theme.GlassCard,
            border = BorderStroke(1.dp, Danger.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Clear All Screen Time History",
                    color = Danger,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun AICoachSubScreen(
    coachPersonality: String,
    onCoachPersonalityChange: (String) -> Unit,
    insightDeliveryFrequency: String,
    onInsightDeliveryChange: (String) -> Unit,
    assistantTone: String,
    onAssistantToneChange: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LumetrixTokens.ScreenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI Coach Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Text(
            text = "COACH PERSONALITY",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = TextSecondary.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val personalities = listOf("Mindful", "Encouraging", "Strict")
            personalities.forEach { personality ->
                val isSelected = coachPersonality == personality
                val colorAccent = when (personality) {
                    "Mindful" -> Success
                    "Encouraging" -> AccentSecondary
                    else -> Danger
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) colorAccent.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                        .border(1.dp, if (isSelected) colorAccent else Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .clickable { onCoachPersonalityChange(personality) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = personality,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Text(
            text = "DELIVERY FREQUENCY",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = TextSecondary.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val options = listOf("Daily", "Weekly", "On Demand")
            options.forEach { opt ->
                val isSelected = insightDeliveryFrequency == opt
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) AccentPrimary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                        .border(1.dp, if (isSelected) AccentPrimary else Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .clickable { onInsightDeliveryChange(opt) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = opt,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Text(
            text = "ASSISTANT TONE",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = TextSecondary.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tones = listOf("Empathetic", "Analytical", "Motivating")
            tones.forEach { tone ->
                val isSelected = assistantTone == tone
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) AccentSecondary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                        .border(1.dp, if (isSelected) AccentSecondary else Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .clickable { onAssistantToneChange(tone) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tone,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutSubScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LumetrixTokens.ScreenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("About Lumetrix", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = com.lumetrix.statsmanager.ui.theme.GlassCard,
            border = BorderStroke(1.dp, GlassCardBorder)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon design placeholder shape
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(AccentPrimary, AccentSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("L", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Lumetrix", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Version 1.2.5 (Premium Edition)", style = MaterialTheme.typography.bodyMedium, color = AccentSecondary)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Lumetrix is an advanced agentic stats manager and digital wellbeing system designed to reclaim your time, track focus, analyze sleep insights, and optimize personal daily routines.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = com.lumetrix.statsmanager.ui.theme.GlassCard,
            border = BorderStroke(1.dp, GlassCardBorder)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingsRow(
                    icon = Icons.Outlined.Info,
                    title = "Terms of Service",
                    onClick = {}
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Outlined.Info,
                    title = "Privacy Policy",
                    onClick = {}
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Outlined.Info,
                    title = "Website & Documentation",
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun SettingsCategorySection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = TextSecondary.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = com.lumetrix.statsmanager.ui.theme.GlassCard,
            border = BorderStroke(1.dp, GlassCardBorder)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val scope = remember { ColumnScope() }
                scope.content()
            }
        }
    }
}

// Scope helper to enable horizontal rows inside column blocks
private class ColumnScope

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    value: String? = null,
    showPurpleDot: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentPrimary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            if (showPurpleDot) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AccentSecondary)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = Color.White.copy(alpha = 0.05f),
        thickness = 1.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    )
}
