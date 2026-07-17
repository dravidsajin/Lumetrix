package com.lumetrix.statsmanager.ui.dashboard

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.Animatable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumetrix.statsmanager.domain.model.AppCategory
import com.lumetrix.statsmanager.domain.model.AppUsageItem
import com.lumetrix.statsmanager.domain.model.TimelineEvent
import com.lumetrix.statsmanager.ui.components.AppUsageCard
import com.lumetrix.statsmanager.ui.components.GlassCard
import com.lumetrix.statsmanager.ui.components.GradientGlassCard
import com.lumetrix.statsmanager.ui.components.NeonLineChart
import com.lumetrix.statsmanager.ui.components.ScreenHeader
import com.lumetrix.statsmanager.ui.components.StatCard
import com.lumetrix.statsmanager.ui.permissions.SyncStatusText
import com.lumetrix.statsmanager.ui.permissions.UsageAccessBanner
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.Danger
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary
import com.lumetrix.statsmanager.ui.theme.Warning
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToAppDetails: (String) -> Unit = {},
    onNavigateToSleep: () -> Unit = {},
    onNavigateToApps: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isVisible by remember { mutableStateOf(false) }
    
    var selectedApp by remember { mutableStateOf<AppUsageItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isVisible = true
        }
    }

    if (uiState.isLoading) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(color = AccentPrimary)
        }
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { 50 },
                animationSpec = tween(600)
            ) + fadeIn(animationSpec = tween(600))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = LumetrixTokens.ScreenPadding),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                ScreenHeader(
                    title = "Pulse",
                    subtitle = "Real-time focus and tracking metrics",
                    actionContent = {
                        SyncStatusText(isSyncing = uiState.isSyncing)
                    }
                )

                // Feature 1: Historical Day Browser
                DateSelectorHeader(
                    selectedDate = uiState.selectedDate,
                    isViewingPastDay = uiState.isViewingPastDay,
                    onPreviousDay = { viewModel.goToPreviousDay() },
                    onNextDay = { viewModel.goToNextDay() },
                    onTodayClick = { viewModel.goToToday() },
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing),
                ) {
                    if (!uiState.hasUsageAccess) {
                        UsageAccessBanner(onGrantClick = { viewModel.openUsageAccessSettings(context) })
                    }

                    // 1. Digital Score Card
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowBorder = true
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ScoreProgressRing(score = uiState.focusScore)

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val delta = uiState.focusScoreDelta ?: 0
                                val titleText = when {
                                    uiState.focusScore >= 80 -> "Great job!"
                                    uiState.focusScore >= 60 -> "Balanced day"
                                    else -> "Keep it up!"
                                }
                                val subtitleText = when {
                                    delta > 0 -> "You're $delta% better than yesterday."
                                    delta < 0 -> "You're ${abs(delta)}% lower than yesterday."
                                    else -> "Steady focus performance."
                                }

                                Text(
                                    text = titleText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = subtitleText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                SparklineChart(
                                    scores = uiState.sparklineScores,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                )
                            }
                        }
                    }

                    // 2. 2x3 Grid of Compact Metrics Cards
                    Column(verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)
                        ) {
                            CompactMetricCard(
                                label = "Screen Time",
                                value = uiState.totalScreenTimeLabel,
                                accentColor = AccentPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            CompactMetricCard(
                                label = "Unlocks",
                                value = uiState.unlockCount.toString(),
                                accentColor = Success,
                                modifier = Modifier.weight(1f)
                            )
                            CompactMetricCard(
                                label = "Notifications",
                                value = uiState.notificationCount.toString(),
                                accentColor = Danger,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)
                        ) {
                            CompactMetricCard(
                                label = "Focus Time",
                                value = uiState.focusTimeLabel,
                                accentColor = AccentSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            CompactMetricCard(
                                label = "Sleep",
                                value = uiState.sleepLabel,
                                accentColor = Color(0xFFFFB84D),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onNavigateToSleep() }
                            )
                            CompactMetricCard(
                                label = "Mood",
                                value = uiState.moodLabel,
                                accentColor = Success,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 3. Most Used Apps Row
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Most Used Apps",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "View All",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AccentPrimary,
                                    modifier = Modifier.clickable {
                                        onNavigateToApps()
                                    }
                                )
                            }

                            if (uiState.topApps.isEmpty()) {
                                Text(
                                    text = if (uiState.hasUsageAccess) "No app usage recorded yet." else "Grant usage access to see apps.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                )
                            } else {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(uiState.topApps) { app ->
                                        HorizontalAppUsageItem(
                                            app = app,
                                            onClick = { onNavigateToAppDetails(app.packageName) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Today's Timeline
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (uiState.isViewingPastDay) "Daily Timeline" else "Today's Timeline",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "View All",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AccentPrimary,
                                    modifier = Modifier.clickable {
                                        onNavigateToApps()
                                    }
                                )
                            }

                            if (uiState.timelineEvents.isEmpty()) {
                                Text(
                                    text = "No timeline events recorded on this day.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                )
                            } else {
                                Column {
                                    uiState.timelineEvents.forEachIndexed { index, event ->
                                        TimelineRow(
                                            event = event,
                                            isFirst = index == 0,
                                            isLast = index == uiState.timelineEvents.lastIndex,
                                            onClick = { onNavigateToAppDetails(event.packageName) }
                                        )
                                        if (index < uiState.timelineEvents.lastIndex) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 52.dp)
                                                    .height(1.dp)
                                                    .background(Color.White.copy(alpha = 0.05f))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }

        if (selectedApp != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedApp = null },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(LumetrixTokens.ScreenPadding)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = selectedApp?.appName ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    
                    TextButton(
                        onClick = {
                            selectedApp?.packageName?.let { onNavigateToAppDetails(it) }
                            selectedApp = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Detailed Stats", color = AccentPrimary)
                    }

                    TextButton(
                        onClick = {
                            selectedApp?.packageName?.let { viewModel.cycleAppCategory(it) }
                            selectedApp = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Category", color = TextSecondary)
                    }
                    
                    TextButton(
                        onClick = {
                            Toast.makeText(context, "Added to Focus list (Coming Soon)", Toast.LENGTH_SHORT).show()
                            selectedApp = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add to Focus Blocker", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactMetricCard(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(accentColor)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HorizontalAppUsageItem(
    app: AppUsageItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        AppIcon(
            packageName = app.packageName,
            fallbackLetter = app.appName.firstOrNull()?.uppercase() ?: "?",
            categoryColor = app.categoryColor,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = app.durationLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TimelineRow(
    event: TimelineEvent,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(androidx.compose.foundation.layout.IntrinsicSize.Min)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val startY = if (isFirst) size.height / 2f else 0f
                val endY = if (isLast) size.height / 2f else size.height
                drawLine(
                    color = Color.White.copy(alpha = 0.12f),
                    start = androidx.compose.ui.geometry.Offset(size.width / 2f, startY),
                    end = androidx.compose.ui.geometry.Offset(size.width / 2f, endY),
                    strokeWidth = 2.dp.toPx()
                )
            }
            AppIcon(
                packageName = event.packageName,
                fallbackLetter = event.appName.firstOrNull()?.uppercase() ?: "?",
                categoryColor = event.categoryColor,
                modifier = Modifier.size(36.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.appName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "${event.startTimeLabel} – ${event.endTimeLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Text(
            text = event.durationLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = event.categoryColor
        )
    }
}

@Composable
private fun AppIcon(
    packageName: String,
    fallbackLetter: String,
    categoryColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val iconDrawable = remember(packageName) {
        runCatching {
            context.packageManager.getApplicationIcon(packageName)
        }.getOrNull()
    }

    if (iconDrawable != null) {
        Canvas(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
        ) {
            drawIntoCanvas { canvas ->
                iconDrawable.setBounds(0, 0, size.width.toInt(), size.height.toInt())
                iconDrawable.draw(canvas.nativeCanvas)
            }
        }
    } else {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            color = categoryColor.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.3f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = fallbackLetter,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor,
                )
            }
        }
    }
}

@Composable
private fun ScoreProgressRing(
    score: Int,
    modifier: Modifier = Modifier
) {
    val animatedScore = remember { Animatable(0f) }
    LaunchedEffect(score) {
        animatedScore.animateTo(score.toFloat(), animationSpec = tween(1200))
    }

    Box(
        modifier = modifier.size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(96.dp)) {
            val strokeWidth = 9.dp.toPx()
            
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                style = Stroke(width = strokeWidth)
            )
            
            val sweepGradient = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFFA020F0),
                    Color(0xFF00F5FF),
                    Color(0xFFA020F0)
                )
            )
            
            val sweepAngle = (animatedScore.value / 100f) * 360f
            
            drawArc(
                brush = sweepGradient,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            if (sweepAngle > 0f) {
                val angleRad = Math.toRadians((sweepAngle - 90f).toDouble())
                val centerOffset = size.width / 2
                val radius = centerOffset
                val tipX = centerOffset + radius * Math.cos(angleRad).toFloat()
                val tipY = centerOffset + radius * Math.sin(angleRad).toFloat()
                
                drawCircle(
                    color = Color(0xFF00F5FF),
                    radius = 5.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(tipX, tipY)
                )
                drawCircle(
                    color = Color(0xFF00F5FF).copy(alpha = 0.4f),
                    radius = 9.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(tipX, tipY)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "/100",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SparklineChart(
    scores: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (scores.isEmpty()) return@Canvas
        val width = size.width
        val height = size.height
        val pointsCount = scores.size
        
        val maxScore = scores.maxOrNull()?.coerceAtLeast(100f) ?: 100f
        val minScore = scores.minOrNull()?.coerceAtMost(0f) ?: 0f
        val range = (maxScore - minScore).coerceAtLeast(1f)

        val path = Path()
        val fillPath = Path()
        
        val stepX = width / (pointsCount - 1).coerceAtLeast(1)
        
        scores.forEachIndexed { index, score ->
            val x = index * stepX
            val y = height - ((score - minScore) / range) * height
            
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                val prevX = (index - 1) * stepX
                val prevY = height - ((scores[index - 1] - minScore) / range) * height
                val controlX1 = prevX + stepX / 2f
                val controlY1 = prevY
                val controlX2 = prevX + stepX / 2f
                val controlY2 = y
                
                path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
            }
            if (index == pointsCount - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF10B981).copy(alpha = 0.25f),
                    Color.Transparent
                )
            )
        )

        drawPath(
            path = path,
            color = Color(0xFF10B981),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw glowing active trend point at the latest coordinate
        if (scores.isNotEmpty()) {
            val lastIndex = scores.lastIndex
            val lastX = lastIndex * stepX
            val lastY = height - ((scores[lastIndex] - minScore) / range) * height
            drawCircle(
                color = Color(0xFF10B981),
                radius = 3.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(lastX, lastY)
            )
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = 0.4f),
                radius = 6.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(lastX, lastY)
            )
        }
    }
}
