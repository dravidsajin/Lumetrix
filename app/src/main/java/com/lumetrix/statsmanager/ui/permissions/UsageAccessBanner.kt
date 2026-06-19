package com.lumetrix.statsmanager.ui.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lumetrix.statsmanager.ui.components.GradientGlassCard
import com.lumetrix.statsmanager.ui.components.PremiumPillButton
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary

@Composable
fun UsageAccessBanner(
    onGrantClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GradientGlassCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Usage access required",
                style = MaterialTheme.typography.titleMedium,
                color = AccentSecondary,
            )
            Text(
                text = "Lumetrix needs permission to read app usage so it can show screen time and focus stats. Your data stays on this device.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Text(
                text = "1. Tap Grant access below\n2. Find Lumetrix in the list\n3. Turn on Permit usage access",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            PremiumPillButton(
                text = "Grant access",
                onClick = onGrantClick,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
fun SyncStatusText(
    isSyncing: Boolean,
    modifier: Modifier = Modifier,
) {
    if (isSyncing) {
        Text(
            text = "Syncing usage data…",
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
            modifier = modifier,
        )
    }
}
