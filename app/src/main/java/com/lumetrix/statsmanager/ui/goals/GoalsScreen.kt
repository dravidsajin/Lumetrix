package com.lumetrix.statsmanager.ui.goals

import android.widget.Toast
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.GlassCardBorder
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary

@Composable
fun GoalsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    goalsViewModel: GoalsViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("Daily") }
    var selectedGoalDetail by remember { mutableStateOf<GoalItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val allGoals by goalsViewModel.goalsState.collectAsState()

    // Sync selected goal details if it is currently open
    val currentGoalDetail = selectedGoalDetail?.let { current ->
        allGoals.firstOrNull { it.id == current.id }
    }

    if (currentGoalDetail != null) {
        GoalDetailsSubScreen(
            goal = currentGoalDetail,
            onBack = { selectedGoalDetail = null },
            onToggleCheck = { dayIdx -> goalsViewModel.toggleGoalCheck(currentGoalDetail.id, dayIdx) }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = LumetrixTokens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary)
                }
                Text("Goals", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Outlined.Add, "Add", tint = TextPrimary)
                }
            }

            // Tab pills: Daily / Weekly / Long Term
            val tabs = listOf("Daily", "Weekly", "Long Term")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) AccentSecondary.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (isSelected) AccentSecondary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(50))
                            .clickable { selectedTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(tab, style = MaterialTheme.typography.labelMedium, color = if (isSelected) AccentSecondary else TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Goals list
            val categoryKey = when (selectedTab) { 
                "Daily" -> "daily"
                "Weekly" -> "weekly"
                else -> "longterm" 
            }
            val filteredGoals = allGoals.filter { it.category == categoryKey }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "$selectedTab Goals",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (filteredGoals.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.EmojiEvents, null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                            Text("No goals in this category yet", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }
                } else {
                    filteredGoals.forEach { goal ->
                        when (goal.type) {
                            "steps" -> StepCounterGoalCard(
                                goal = goal, 
                                onClick = { selectedGoalDetail = goal }
                            )
                            "boolean" -> BooleanGoalCard(
                                goal = goal, 
                                onClick = { selectedGoalDetail = goal }
                            )
                            else -> ProgressGoalCard(
                                goal = goal, 
                                onClick = { selectedGoalDetail = goal },
                                onIncrement = { goalsViewModel.incrementGoalProgress(goal.id, 5f) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // + Add Goal button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(Success.copy(alpha = 0.15f), Success.copy(alpha = 0.05f))))
                        .border(1.dp, Success.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { showAddDialog = true }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Add, null, tint = Success, modifier = Modifier.size(18.dp))
                        Text("Add Goal", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Success)
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        if (showAddDialog) {
            AddGoalDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, desc, emoji, limit, cat ->
                    goalsViewModel.addGoal(title, desc, emoji, limit, cat)
                    showAddDialog = false
                    Toast.makeText(context, "Goal added!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

// Helper to parse Hex color strings
private fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        AccentPrimary
    }
}

// ── Goal Cards ──

@Composable
private fun ProgressGoalCard(
    goal: GoalItem, 
    onClick: () -> Unit,
    onIncrement: () -> Unit
) {
    val pct = ((goal.current / goal.target) * 100).toInt().coerceIn(0, 100)
    val color = parseHexColor(goal.accentColorHex)
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = com.lumetrix.statsmanager.ui.theme.GlassCard,
        border = BorderStroke(1.dp, GlassCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(goal.icon, style = MaterialTheme.typography.titleLarge)
                    Column {
                        Text(goal.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(goal.description, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1)
                    }
                }
                
                // Direct progress increment button
                if (goal.current < goal.target && (goal.id.startsWith("custom_") || goal.id == "water_daily")) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(color.copy(alpha = 0.15f))
                            .clickable { onIncrement() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("+5m", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${goal.current.toCleanString()} / ${goal.target.toCleanString()} ${goal.unit}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text("$pct%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
            }
            LinearProgressIndicator(
                progress = { (goal.current / goal.target).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = color,
                trackColor = color.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
private fun StepCounterGoalCard(goal: GoalItem, onClick: () -> Unit) {
    val progress = (goal.current / goal.target).coerceIn(0f, 1f)
    val color = parseHexColor(goal.accentColorHex)
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = com.lumetrix.statsmanager.ui.theme.GlassCard,
        border = BorderStroke(1.dp, GlassCardBorder)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(goal.icon, style = MaterialTheme.typography.titleLarge)
                Column {
                    Text(goal.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${goal.current.toInt()} / ${goal.target.toInt()} ${goal.unit}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
            // Circular progress ring
            Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(56.dp)) {
                    drawArc(color = Color.White.copy(alpha = 0.06f), 0f, 360f, false, style = Stroke(6.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(color = color, startAngle = -90f, sweepAngle = progress * 360f, useCenter = false, style = Stroke(6.dp.toPx(), cap = StrokeCap.Round))
                }
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }
    }
}

@Composable
private fun BooleanGoalCard(goal: GoalItem, onClick: () -> Unit) {
    val isComplete = goal.current >= goal.target
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = com.lumetrix.statsmanager.ui.theme.GlassCard,
        border = BorderStroke(1.dp, if (isComplete) Success.copy(alpha = 0.3f) else GlassCardBorder)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(goal.icon, style = MaterialTheme.typography.titleLarge)
                Column {
                    Text(goal.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(goal.description, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
            if (isComplete) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Success.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Check, null, tint = Success, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ── Goal Details Sub-Screen ──

@Composable
private fun GoalDetailsSubScreen(
    goal: GoalItem, 
    onBack: () -> Unit,
    onToggleCheck: (Int) -> Unit
) {
    val pct = ((goal.current / goal.target) * 100).toInt().coerceIn(0, 100)
    val color = parseHexColor(goal.accentColorHex)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LumetrixTokens.ScreenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Goal Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        // Goal title card
        Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Text(goal.icon, style = MaterialTheme.typography.headlineSmall)
                }
                Column {
                    Text(goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(goal.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }

        // Progress card
        Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Progress", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${goal.current.toCleanString()} / ${goal.target.toCleanString()} ${goal.unit}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text("$pct%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
                }
                LinearProgressIndicator(
                    progress = { (goal.current / goal.target).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = color,
                    trackColor = color.copy(alpha = 0.1f)
                )
            }
        }

        // Weekly calendar
        Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("This Week", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                val days = listOf("M", "T", "W", "T", "F", "S", "S")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    days.forEachIndexed { idx, day ->
                        val checked = goal.weekChecks.getOrNull(idx) ?: false
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(day, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape)
                                    .background(if (checked) Success.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(1.dp, if (checked) Success.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f), CircleShape)
                                    .clickable { onToggleCheck(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (checked) Icon(Icons.Outlined.Check, null, tint = Success, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Statistics card
        Surface(shape = RoundedCornerShape(16.dp), color = com.lumetrix.statsmanager.ui.theme.GlassCard, border = BorderStroke(1.dp, GlassCardBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Statistics", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem("🔥", "Streak", "${goal.streak} days")
                    StatItem("🏆", "Best", "${goal.bestStreak} days")
                    StatItem("✅", "Total", "${goal.totalCompleted}")
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun StatItem(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(emoji, style = MaterialTheme.typography.titleMedium)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

// ── Add Goal Dialog ──

@Composable
private fun AddGoalDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, Float, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🎯") }
    var limit by remember { mutableStateOf("30") }
    var category by remember { mutableStateOf("daily") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Goal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Goal Title") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = Color.White.copy(alpha = 0.15f), focusedLabelColor = AccentPrimary, unfocusedLabelColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = Color.White.copy(alpha = 0.15f), focusedLabelColor = AccentPrimary, unfocusedLabelColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary), modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = emoji, onValueChange = { emoji = it }, label = { Text("Icon") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = Color.White.copy(alpha = 0.15f), focusedLabelColor = AccentPrimary, unfocusedLabelColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = limit, onValueChange = { limit = it }, label = { Text("Target") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = Color.White.copy(alpha = 0.15f), focusedLabelColor = AccentPrimary, unfocusedLabelColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary), modifier = Modifier.weight(1f))
                }
                // Category selector
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("daily" to "Daily", "weekly" to "Weekly", "longterm" to "Long Term").forEach { (key, label) ->
                        val sel = category == key
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (sel) AccentSecondary.copy(alpha = 0.15f) else Color.Transparent).border(1.dp, if (sel) AccentSecondary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp)).clickable { category = key }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text(label, style = MaterialTheme.typography.labelMedium, color = if (sel) AccentSecondary else TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { if (title.isNotBlank()) onConfirm(title, desc, emoji, limit.toFloatOrNull() ?: 30f, category) }, colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)) { Text("Add Goal", color = Color.Black, fontWeight = FontWeight.Bold) } },
        dismissButton = { Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) { Text("Cancel", color = TextSecondary) } },
        containerColor = Color(0xFF1E1A3C)
    )
}

// ── Helpers ──

private fun Float.toCleanString(): String {
    return if (this == this.toLong().toFloat()) this.toLong().toString()
    else String.format("%.1f", this)
}
