package com.lumetrix.statsmanager.ui.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
                onClick = { Toast.makeText(context, "General settings clicked", Toast.LENGTH_SHORT).show() }
            )
            SettingsDivider()

            SettingsRow(
                icon = Icons.Outlined.Palette,
                title = "Theme",
                value = "Dark",
                showPurpleDot = true,
                onClick = { Toast.makeText(context, "Theme options clicked", Toast.LENGTH_SHORT).show() }
            )
            SettingsDivider()

            SettingsRow(
                icon = Icons.Outlined.Notifications,
                title = "Notifications",
                onClick = { Toast.makeText(context, "Notification preferences clicked", Toast.LENGTH_SHORT).show() }
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
                onClick = { Toast.makeText(context, "Focus settings clicked", Toast.LENGTH_SHORT).show() }
            )
            SettingsDivider()

            SettingsRow(
                icon = Icons.Outlined.Lock,
                title = "Data & Privacy",
                onClick = { Toast.makeText(context, "Privacy options clicked", Toast.LENGTH_SHORT).show() }
            )
            SettingsDivider()

            SettingsRow(
                icon = Icons.Outlined.Person,
                title = "AI Coach",
                onClick = { Toast.makeText(context, "AI Coach configurations clicked", Toast.LENGTH_SHORT).show() }
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
                onClick = { Toast.makeText(context, "Lumetrix version 1.0.0", Toast.LENGTH_SHORT).show() }
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
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
