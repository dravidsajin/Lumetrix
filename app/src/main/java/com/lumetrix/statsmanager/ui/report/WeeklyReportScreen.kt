package com.lumetrix.statsmanager.ui.report

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.GlassCardBorder
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Chat Models
// ─────────────────────────────────────────────────────────────────────────────

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isSummaryCard: Boolean = false,
    val summaryData: WeeklyReportUiState? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// Weekly Report Screen - Chat UI Redesign
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun WeeklyReportScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    weeklyReportViewModel: WeeklyReportViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val state by weeklyReportViewModel.reportState.collectAsState()
    
    val listState = rememberLazyListState()
    var inputQuery by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }

    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "Hi there! I'm your AI Coach. 🔮",
                isUser = false
            ),
            ChatMessage(
                text = "I've analyzed your device usage, focus sessions, and wellness habits. Tap below or ask me any question to get started!",
                isUser = false
            )
        )
    }

    // Scroll to latest message
    LaunchedEffect(chatMessages.size, isTyping) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.lastIndex + if (isTyping) 1 else 0)
        }
    }

    val availableSuggestions = listOf(
        "How was my week?",
        "How can I reduce screen time?",
        "Best time for Deep Work?",
        "What's my Digital Score?"
    )

    fun handleSend(query: String) {
        if (query.trim().isEmpty()) return
        chatMessages.add(ChatMessage(text = query, isUser = true))
        inputQuery = ""
        isTyping = true

        coroutineScope.launch {
            delay(1200) // Simulated AI typing thinking delay
            isTyping = false
            
            val lowercaseQuery = query.lowercase().trim()
            
            // Helper values from state
            val screenTimeItem = state.summaryItems.find { it.label == "Screen Time" }
            val screenTimeLabel = screenTimeItem?.valueLabel ?: ""
            val screenTimeDiff = screenTimeLabel.substringAfter("(").substringBefore(")")
            val screenTimeText = when {
                screenTimeDiff.contains("↑") -> "Your screen time increased by ${screenTimeDiff.replace("↑", "").trim()} compared to last week."
                screenTimeDiff.contains("↓") -> "Your screen time decreased by ${screenTimeDiff.replace("↓", "").trim()} compared to last week."
                else -> "Your screen time remained steady compared to last week."
            }

            val focusTimeLabel = state.summaryItems.find { it.label == "Focus Time" }?.valueLabel ?: ""
            val sleepLabel = state.summaryItems.find { it.label == "Sleep" }?.valueLabel ?: ""
            val unlockLabel = state.summaryItems.find { it.label == "Unlocks" }?.valueLabel ?: ""
            val notificationLabel = state.summaryItems.find { it.label == "Notifications" }?.valueLabel ?: ""

            val response = when {
                lowercaseQuery.contains("week") || lowercaseQuery.contains("summary") || lowercaseQuery.contains("report") -> {
                    chatMessages.add(
                        ChatMessage(
                            text = "Here's your weekly summary 👇",
                            isUser = false,
                            isSummaryCard = true,
                            summaryData = state
                        )
                    )
                    return@launch
                }
                
                lowercaseQuery.contains("screen") || lowercaseQuery.contains("usage") || lowercaseQuery.contains("hour") || lowercaseQuery.contains("time") -> {
                    "Your weekly average screen time is **$screenTimeLabel**. 📱\n\n" +
                    if (screenTimeLabel.contains("↑")) {
                        "This is an increase compared to last week. I recommend creating an App Chain Rule for your most-used distracting apps to help pull this down!"
                    } else {
                        "Fantastic job keeping your screen time controlled compared to last week! Keep it up!"
                    }
                }
                
                lowercaseQuery.contains("sleep") || lowercaseQuery.contains("night") || lowercaseQuery.contains("bed") -> {
                    "Your average sleep duration this week is **$sleepLabel** (calculated from your overnight offline gaps). 😴\n\n" +
                    "Try to activate the bedtime focus mode 30 minutes before sleep to minimize late-night interruptions."
                }
                
                lowercaseQuery.contains("unlock") || lowercaseQuery.contains("pickup") || lowercaseQuery.contains("open") -> {
                    "You unlocked your device **$unlockLabel** times on average this week. 🔓\n\n" +
                    "Frequent pickups break focus. Try setting a batch-delivery schedule for your notifications to minimize pings."
                }
                
                lowercaseQuery.contains("notif") || lowercaseQuery.contains("alert") || lowercaseQuery.contains("ping") || lowercaseQuery.contains("message") -> {
                    "You received **$notificationLabel** on average this week. 📬\n\n" +
                    "Reducing notification frequency is a great first step to improving your overall deep focus score!"
                }
                
                lowercaseQuery.contains("focus") || lowercaseQuery.contains("deep") || lowercaseQuery.contains("productiv") -> {
                    val productivityHour = state.insights.find { it.emoji == "⏰" || it.text.lowercase().contains("productivity") }?.text 
                        ?: "Your peak productivity slot is early morning."
                    "You logged **$focusTimeLabel** of deep focus sessions on average. 🎯\n\n" +
                    "Additionally: $productivityHour"
                }
                
                lowercaseQuery.contains("score") || lowercaseQuery.contains("digital balance") || lowercaseQuery.contains("wellness") || lowercaseQuery.contains("rating") -> {
                    val scoreAdvice = if (state.digitalScore >= 80) {
                        "That's a stellar rating! You are doing great at balancing productivity and device usage. 🔥"
                    } else {
                        "There is room for improvement. Try completing 1-2 more daily focus sessions to raise your score. 🌿"
                    }
                    "Your overall Digital Balance Score is **${state.digitalScore}/100** this week! 📈\n\n$scoreAdvice"
                }
                
                lowercaseQuery.contains("achieve") || lowercaseQuery.contains("streak") || lowercaseQuery.contains("win") || lowercaseQuery.contains("best") || lowercaseQuery.contains("done") -> {
                    val achievementsList = state.achievements.joinToString("\n") { "${it.emoji} ${it.text}" }
                    "Here are your key achievements for this week: 🌟\n\n$achievementsList"
                }
                
                lowercaseQuery.contains("hello") || lowercaseQuery.contains("hi") || lowercaseQuery.contains("hey") || lowercaseQuery.contains("greetings") || lowercaseQuery.contains("who are you") || lowercaseQuery.contains("coach") -> {
                    "Hello! I am your personal AI Coach. 🔮\n\nI analyze your weekly data (sleep, screen time, notifications, focus) to help you build balanced habit loops. What would you like to know?"
                }
                
                lowercaseQuery.contains("thanks") || lowercaseQuery.contains("thank you") || lowercaseQuery.contains("awesome") || lowercaseQuery.contains("great") || lowercaseQuery.contains("cool") -> {
                    "You're very welcome! I'm always here to help you stay mindful. Let's make today a highly focused day! 🚀"
                }
                
                else -> {
                    "I see! Developing positive habits is a journey. Try asking me:\n" +
                    "• *'How was my week?'*\n" +
                    "• *'How can I reduce screen time?'*\n" +
                    "• *'Best time for Deep Work?'*\n" +
                    "• *'What is my digital score?'*"
                }
            }
            
            chatMessages.add(ChatMessage(text = response, isUser = false))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color(0xFF0D0D14)) // Dark immersive theme matching mockup
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LumetrixTokens.ScreenPadding, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sparkle Icon
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = AccentSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Coach",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            }
            // Sparkles icon matching top right of mockup
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }

        // ── Chat Area ───────────────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = LumetrixTokens.ScreenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(chatMessages, key = { it.id }) { message ->
                if (message.isUser) {
                    UserChatBubble(message.text)
                } else {
                    if (message.isSummaryCard && message.summaryData != null) {
                        SummaryCardBubble(message.summaryData)
                    } else {
                        AiChatBubble(message.text)
                    }
                }
            }
            
            if (isTyping) {
                item {
                    AiTypingIndicator()
                }
            }
        }

        // ── Quick Suggestions Row ───────────────────────────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = LumetrixTokens.ScreenPadding, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(availableSuggestions) { suggestion ->
                SuggestionChip(
                    label = suggestion,
                    onClick = { handleSend(suggestion) }
                )
            }
        }

        // ── Chat Input Panel ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LumetrixTokens.ScreenPadding)
                .padding(bottom = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Voice Mic Button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { Toast.makeText(context, "Voice input not supported", Toast.LENGTH_SHORT).show() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = "Voice Input",
                    tint = TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Input TextField
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = {
                    Text(
                        "Ask me anything...",
                        color = TextSecondary.copy(alpha = 0.8f),
                        fontSize = 15.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.03f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                    focusedBorderColor = Color.White.copy(alpha = 0.12f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            // Send Button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                AccentPrimary,
                                AccentPrimary.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .clickable {
                        if (inputQuery.trim().isNotEmpty()) {
                            handleSend(inputQuery)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UI Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun UserChatBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
                .background(AccentPrimary.copy(alpha = 0.85f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun AiChatBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun SummaryCardBubble(state: WeeklyReportUiState) {
    // 1. Dynamic Screen Time bullet
    val screenTimeItem = state.summaryItems.find { it.label == "Screen Time" }
    val screenTimeLabel = screenTimeItem?.valueLabel ?: ""
    val screenTimeDiff = screenTimeLabel.substringAfter("(").substringBefore(")")
    val screenTimeText = when {
        screenTimeDiff.contains("↑") -> "Your screen time increased by ${screenTimeDiff.replace("↑", "").trim()} compared to last week."
        screenTimeDiff.contains("↓") -> "Your screen time decreased by ${screenTimeDiff.replace("↓", "").trim()} compared to last week."
        else -> "Your screen time remained steady compared to last week."
    }

    // 2. Dynamic Productivity bullet
    val productivityText = state.insights.find { it.emoji == "⏰" || it.text.lowercase().contains("productivity") }?.text
        ?: "You are most productive during early morning slots."

    // 3. Dynamic Notifications bullet
    val notificationItem = state.summaryItems.find { it.label == "Notifications" }
    val notificationLabel = notificationItem?.valueLabel ?: ""
    val notificationDiff = notificationLabel.substringAfter("(").substringBefore(")")
    val notificationText = when {
        notificationDiff.contains("↑") -> "You received ${notificationDiff.replace("↑", "").trim()} more notifications."
        notificationDiff.contains("↓") -> "You received ${notificationDiff.replace("↓", "").trim()} fewer notifications."
        else -> "Your notifications remained stable this week."
    }

    // 4. Dynamic Focus Time bullet
    val focusItem = state.summaryItems.find { it.label == "Focus Time" }
    val focusLabel = focusItem?.valueLabel ?: ""
    val focusDiff = focusLabel.substringAfter("(").substringBefore(")")
    val focusText = when {
        focusDiff.contains("↑") -> "Focus time improved by ${focusDiff.replace("↑", "").trim()} compared to last week."
        focusDiff.contains("↓") -> "Focus time decreased by ${focusDiff.replace("↓", "").trim()} compared to last week."
        else -> "Your focus session volume was stable this week."
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.04f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Here's your weekly summary 👇",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "• $screenTimeText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                    Text(
                        text = "• $productivityText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                    Text(
                        text = "• $notificationText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                    Text(
                        text = "• $focusText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                }

                Text(
                    text = if (state.digitalScore >= 80) "Overall, you're on the right track!\nKeep going 🔥" else "A few small tweaks will boost your balance!\nLet's do it 🌿",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun AiTypingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CircularProgressIndicator(
                    color = AccentSecondary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "AI Coach is typing...",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun SuggestionChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
    }
}
