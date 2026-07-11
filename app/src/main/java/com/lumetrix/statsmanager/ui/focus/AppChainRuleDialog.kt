package com.lumetrix.statsmanager.ui.focus

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lumetrix.statsmanager.domain.model.SimpleAppInfo
import com.lumetrix.statsmanager.ui.components.PremiumPillButton
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.BackgroundSecondary
import com.lumetrix.statsmanager.ui.theme.Danger
import com.lumetrix.statsmanager.ui.theme.Divider
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppChainRuleDialog(
    availableApps: List<SimpleAppInfo>,
    onDismiss: () -> Unit,
    onConfirm: (SimpleAppInfo, Int, SimpleAppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    var gateSearch by remember { mutableStateOf("") }
    var targetSearch by remember { mutableStateOf("") }
    var durationMin by remember { mutableStateOf(10) }
    
    var selectedGateApp by remember { mutableStateOf<SimpleAppInfo?>(null) }
    var selectedTargetApp by remember { mutableStateOf<SimpleAppInfo?>(null) }

    val filteredGateApps = remember(gateSearch, availableApps) {
        availableApps.filter { it.appName.contains(gateSearch, ignoreCase = true) }
    }
    val filteredTargetApps = remember(targetSearch, availableApps) {
        availableApps.filter { it.appName.contains(targetSearch, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BackgroundSecondary,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Divider) },
        shape = RoundedCornerShape(topStart = LumetrixTokens.CardRadius, topEnd = LumetrixTokens.CardRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LumetrixTokens.ScreenPadding)
                .padding(bottom = 40.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Chain Rule",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Text(
                text = "Chain two apps: you must use the Gate App for the specified duration before the Target App unlocks today.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            // 1. SELECT GATE APP
            Text(
                text = "Step 1: Pick Gate App (Productive App)",
                style = MaterialTheme.typography.labelLarge,
                color = AccentPrimary,
                fontWeight = FontWeight.Bold
            )

            if (selectedGateApp == null) {
                OutlinedTextField(
                    value = gateSearch,
                    onValueChange = { gateSearch = it },
                    placeholder = { Text("Search app...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextSecondary,
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = Divider,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                
                Box(modifier = Modifier.height(140.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        filteredGateApps.take(15).forEach { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedGateApp = app }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = app.appName, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Success.copy(alpha = 0.1f))
                        .border(1.dp, Success.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📖 Gate: ${selectedGateApp!!.appName}", color = Success, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Change",
                        modifier = Modifier.clickable { selectedGateApp = null },
                        color = AccentPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2. SET DURATION
            Text(
                text = "Step 2: Require Duration (Minutes)",
                style = MaterialTheme.typography.labelLarge,
                color = AccentPrimary,
                fontWeight = FontWeight.Bold
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Use Gate App for", color = TextSecondary)
                    Text(text = "$durationMin minutes", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = durationMin.toFloat(),
                    onValueChange = { durationMin = it.toInt() },
                    valueRange = 5f..60f,
                    steps = 11, // 5, 10, 15, 20...
                    colors = SliderDefaults.colors(
                        activeTrackColor = AccentPrimary,
                        inactiveTrackColor = Divider,
                        thumbColor = AccentSecondary
                    )
                )
            }

            // 3. SELECT TARGET APP
            Text(
                text = "Step 3: Pick Target App (Distracting App)",
                style = MaterialTheme.typography.labelLarge,
                color = AccentPrimary,
                fontWeight = FontWeight.Bold
            )

            if (selectedTargetApp == null) {
                OutlinedTextField(
                    value = targetSearch,
                    onValueChange = { targetSearch = it },
                    placeholder = { Text("Search app...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextSecondary,
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = Divider,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                
                Box(modifier = Modifier.height(140.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        filteredTargetApps.take(15).forEach { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedTargetApp = app }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = app.appName, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Danger.copy(alpha = 0.1f))
                        .border(1.dp, Danger.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📱 Target: ${selectedTargetApp!!.appName}", color = Danger, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Change",
                        modifier = Modifier.clickable { selectedTargetApp = null },
                        color = AccentPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Button
            val isEnabled = selectedGateApp != null && selectedTargetApp != null
            PremiumPillButton(
                text = "Save Chain Rule",
                onClick = {
                    if (isEnabled) {
                        onConfirm(selectedGateApp!!, durationMin, selectedTargetApp!!)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
