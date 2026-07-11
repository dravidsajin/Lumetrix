package com.lumetrix.statsmanager.ui.apps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumetrix.statsmanager.domain.model.AppCategory
import com.lumetrix.statsmanager.ui.components.AppUsageCard
import com.lumetrix.statsmanager.ui.components.GlassCard
import com.lumetrix.statsmanager.ui.components.ScreenHeader
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.Danger
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary
import com.lumetrix.statsmanager.ui.theme.Warning

@Composable
fun AppsScreen(
    onNavigateToAppDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentPrimary)
        }
        return
    }

    // Build segments dynamically from the user's top apps
    val donutSegments = remember(uiState.allApps) {
        val totalMs = uiState.allApps.sumOf { it.durationMs }.coerceAtLeast(1L)
        val topApps = uiState.allApps.take(5)
        val topSum = topApps.sumOf { it.durationMs }
        val otherMs = (totalMs - topSum).coerceAtLeast(0L)
        
        val colors = listOf(
            AccentSecondary,
            Warning,
            Success,
            AccentPrimary,
            Danger,
            Color(0xFF90A4AE)
        )
        
        val list = topApps.mapIndexed { index, app ->
            AppDonutSegment(
                appName = app.appName,
                percentage = (app.durationMs.toFloat() / totalMs * 100f),
                color = colors.getOrElse(index) { colors.last() }
            )
        }.toMutableList()
        
        if (otherMs > 0) {
            list.add(
                AppDonutSegment(
                    appName = "Others",
                    percentage = (otherMs.toFloat() / totalMs * 100f),
                    color = colors.last()
                )
            )
        }
        list
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LumetrixTokens.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        ScreenHeader(
            title = "All Apps",
            subtitle = "${uiState.totalTimeLabel} spent today"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f),
            verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)
        ) {
            // Card 1: App Usage Donut Chart
            if (uiState.allApps.isNotEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "App Usage",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AppsDonutChart(
                                    segments = donutSegments,
                                    centerLabel = uiState.totalTimeLabel,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Column(
                                    modifier = Modifier.weight(1.2f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    donutSegments.forEach { segment ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(segment.color)
                                                )
                                                Text(
                                                    text = segment.appName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextPrimary,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                    modifier = Modifier.width(90.dp)
                                                )
                                            }
                                            Text(
                                                text = "${segment.percentage.toInt()}%",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextSecondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Card 2: Unlocks hourly chart
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Unlocks",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${uiState.unlockCount} times",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "▼ 8% vs yesterday",
                                style = MaterialTheme.typography.labelSmall,
                                color = Success,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        UnlocksHourlyChart(
                            unlockCount = uiState.unlockCount,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Card 3: Notifications line chart
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Notifications",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${uiState.notificationCount}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "▼ 15% vs yesterday",
                                style = MaterialTheme.typography.labelSmall,
                                color = Success,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        MiniWaveLineChart(
                            color = Color(0xFF8126F2),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Category Filter pills selector
            item {
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val isAllSelected = uiState.selectedCategory == null
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAllSelected) AccentPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { viewModel.selectCategory(null) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "All",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isAllSelected) AccentPrimary else TextSecondary
                            )
                        }

                        AppCategory.entries.forEach { cat ->
                            val isSelected = uiState.selectedCategory == cat
                            val catColor = when (cat) {
                                AppCategory.Productive -> Success
                                AppCategory.Neutral -> Warning
                                AppCategory.Distracting -> Danger
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) catColor.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { viewModel.selectCategory(cat) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) catColor else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
            
            // App usage list items
            if (uiState.displayApps.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (!uiState.hasUsageAccess) "Grant usage access to see apps." else "No apps found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                }
            } else {
                items(uiState.displayApps) { app ->
                    AppUsageCard(
                        appName = app.appName,
                        duration = app.durationLabel,
                        category = app.category.label,
                        categoryColor = app.categoryColor,
                        onClick = { onNavigateToAppDetails(app.packageName) }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp)) // padding for bottom nav
            }
        }
    }
}

private data class AppDonutSegment(
    val appName: String,
    val percentage: Float,
    val color: Color
)

@Composable
private fun AppsDonutChart(
    segments: List<AppDonutSegment>,
    centerLabel: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val strokeWidth = 10.dp.toPx()
            
            // Draw background track
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = 120.dp.toPx() / 2f,
                style = Stroke(width = strokeWidth)
            )
            
            var startAngle = -90f
            segments.forEach { segment ->
                val sweepAngle = (segment.percentage / 100f) * 360f
                drawArc(
                    color = segment.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweepAngle
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "Total",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun MiniWaveLineChart(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.height(70.dp).fillMaxWidth()) {
        val path = androidx.compose.ui.graphics.Path()
        val width = size.width
        val height = size.height
        
        val points = listOf(
            0f to 0.8f,
            0.2f to 0.4f,
            0.4f to 0.7f,
            0.6f to 0.2f, // Peak!
            0.8f to 0.5f,
            1f to 0.3f
        )
        
        path.moveTo(0f, height * points[0].second)
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            val cp1X = (prev.first + curr.first) / 2 * width
            val cp1Y = prev.second * height
            val cp2X = (prev.first + curr.first) / 2 * width
            val cp2Y = curr.second * height
            path.cubicTo(
                cp1X, cp1Y,
                cp2X, cp2Y,
                curr.first * width, curr.second * height
            )
        }
        
        // Draw glowing gradient background fill
        val fillPath = androidx.compose.ui.graphics.Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                listOf(color.copy(alpha = 0.2f), Color.Transparent)
            )
        )
        
        // Draw gradient line stroke
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.5f))),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Draw active glowing neon peak dot
        val peakX = 0.6f * width
        val peakY = 0.2f * height
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = 8.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(peakX, peakY)
        )
        drawCircle(
            color = color,
            radius = 4.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(peakX, peakY)
        )
    }
}

@Composable
private fun UnlocksHourlyChart(
    unlockCount: Int,
    modifier: Modifier = Modifier
) {
    val barHeights = remember(unlockCount) {
        val rand = java.util.Random(42)
        List(24) { (rand.nextFloat() * 0.85f + 0.15f).coerceIn(0.15f, 1f) }
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            barHeights.forEach { heightFraction ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(heightFraction)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(AccentSecondary, AccentPrimary)
                            )
                        )
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("12 AM", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text("6 AM", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text("12 PM", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text("6 PM", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text("12 AM", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}
