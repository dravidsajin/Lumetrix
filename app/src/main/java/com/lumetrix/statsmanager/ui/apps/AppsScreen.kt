package com.lumetrix.statsmanager.ui.apps

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Launch
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumetrix.statsmanager.core.time.DurationFormatter
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
    onBack: (() -> Unit)? = null,
    viewModel: AppsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentPrimary)
        }
        return
    }

    // Build Donut segments dynamically from top apps
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
                color = colors.getOrElse(index) { colors.last() },
                durationMs = app.durationMs
            )
        }.toMutableList()
        
        if (otherMs > 0) {
            list.add(
                AppDonutSegment(
                    appName = "Others",
                    percentage = (otherMs.toFloat() / totalMs * 100f),
                    color = colors.last(),
                    durationMs = otherMs
                )
            )
        }
        list
    }

    // Dynamic Category timing calculation
    val (productiveLabel, neutralLabel, distractingLabel) = remember(uiState.allApps) {
        val prodMs = uiState.allApps.filter { it.category == AppCategory.Productive }.sumOf { it.durationMs }
        val neutrMs = uiState.allApps.filter { it.category == AppCategory.Neutral }.sumOf { it.durationMs }
        val distMs = uiState.allApps.filter { it.category == AppCategory.Distracting }.sumOf { it.durationMs }
        Triple(
            DurationFormatter.formatShort(prodMs),
            DurationFormatter.formatShort(neutrMs),
            DurationFormatter.formatShort(distMs)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LumetrixTokens.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing),
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            ScreenHeader(
                title = "All Apps",
                subtitle = "${uiState.totalTimeLabel} spent today",
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                Toast.makeText(context, "Filters opened", Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = "Filter",
                    tint = TextPrimary
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f),
            verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing)
        ) {
            // Card 1: App Usage Donut Chart & Legend
            if (uiState.allApps.isNotEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Timer,
                                    contentDescription = null,
                                    tint = AccentSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "App Usage",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            
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
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    donutSegments.forEach { segment ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.width(80.dp),
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Text(
                                                text = "${segment.percentage.toInt()}%",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextSecondary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.06f)))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        Toast.makeText(context, "Opening breakdown", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "View App Breakdown",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AccentPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            
            // Card 2: Unlocks hourly chart (with right-aligned Lock graphic icon)
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Unlocks",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = "${uiState.unlockCount} times",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Icon(imageVector = Icons.Rounded.TrendingDown, contentDescription = null, tint = Success, modifier = Modifier.size(10.dp))
                                    Text(
                                        text = "8% vs yesterday",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Success,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            
                            // Glowing Lock representation (Mockup Right element)
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(
                                        color = AccentPrimary.copy(alpha = 0.12f),
                                        radius = size.minDimension / 2
                                    )
                                    drawCircle(
                                        color = AccentPrimary.copy(alpha = 0.35f),
                                        radius = size.minDimension / 2.6f,
                                        style = Stroke(width = 1.5.dp.toPx())
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = AccentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))
                        
                        UnlocksHourlyChart(
                            unlockDistribution = uiState.unlockDistribution,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Card 3: Notifications line chart (with bottom detail link button)
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Outlined.Notifications, contentDescription = null, tint = Color(0xFF8126F2), modifier = Modifier.size(16.dp))
                            Text(
                                text = "Notifications",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "${uiState.notificationCount}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Icon(imageVector = Icons.Rounded.TrendingDown, contentDescription = null, tint = Success, modifier = Modifier.size(10.dp))
                                    Text(
                                        text = "15% vs yesterday",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Success,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            
                            MiniWaveLineChart(
                                notificationDistribution = uiState.notificationDistribution,
                                color = Color(0xFF8126F2),
                                modifier = Modifier.width(180.dp)
                            )
                        }
                        
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Toast.makeText(context, "Opening notification details", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                    text = "View Notification Details",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AccentPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            
            // Card 4: App Categories Card (Segmented Grid Selector)
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "App Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isAllSelected = uiState.selectedCategory == null
                        
                        // All Apps Button
                        CategorySelectButton(
                            title = "All Apps",
                            value = uiState.totalTimeLabel,
                            icon = Icons.Outlined.Category,
                            iconColor = AccentPrimary,
                            isSelected = isAllSelected,
                            onClick = { viewModel.selectCategory(null) },
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Productive Button
                        CategorySelectButton(
                            title = "Productive",
                            value = productiveLabel,
                            icon = Icons.Outlined.Launch,
                            iconColor = Success,
                            isSelected = uiState.selectedCategory == AppCategory.Productive,
                            onClick = { viewModel.selectCategory(AppCategory.Productive) },
                            modifier = Modifier.weight(1.1f)
                        )
                        
                        // Neutral Button
                        CategorySelectButton(
                            title = "Neutral",
                            value = neutralLabel,
                            icon = Icons.Outlined.Info,
                            iconColor = Warning,
                            isSelected = uiState.selectedCategory == AppCategory.Neutral,
                            onClick = { viewModel.selectCategory(AppCategory.Neutral) },
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Distracting Button
                        CategorySelectButton(
                            title = "Distracting",
                            value = distractingLabel,
                            icon = Icons.Outlined.Lock,
                            iconColor = Danger,
                            isSelected = uiState.selectedCategory == AppCategory.Distracting,
                            onClick = { viewModel.selectCategory(AppCategory.Distracting) },
                            modifier = Modifier.weight(1.1f)
                        )
                    }
                }
            }
            
            // App usage list items below category selection
            if (uiState.displayApps.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
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
                Spacer(modifier = Modifier.height(100.dp)) // Padding for navigation bar/scrolling comfort
            }
        }
    }
}

@Composable
private fun CategorySelectButton(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) iconColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
            .border(
                1.dp,
                if (isSelected) iconColor.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(if (isSelected) iconColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = if (isSelected) iconColor else TextPrimary, modifier = Modifier.size(16.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) TextPrimary else TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) iconColor else TextSecondary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private data class AppDonutSegment(
    val appName: String,
    val percentage: Float,
    val color: Color,
    val durationMs: Long
)

@Composable
private fun AppsDonutChart(
    segments: List<AppDonutSegment>,
    centerLabel: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(100.dp)) {
            val strokeWidth = 9.dp.toPx()
            
            // Draw background track
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = 100.dp.toPx() / 2f,
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
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                fontSize = 16.sp
            )
            Text(
                text = "Total",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun MiniWaveLineChart(
    notificationDistribution: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.height(45.dp)) {
        if (notificationDistribution.none { it < 1f }) return@Canvas
        val path = androidx.compose.ui.graphics.Path()
        val width = size.width
        val height = size.height
        
        val points = if (notificationDistribution.size == 6) {
            listOf(
                0f to notificationDistribution[0],
                0.2f to notificationDistribution[1],
                0.4f to notificationDistribution[2],
                0.6f to notificationDistribution[3],
                0.8f to notificationDistribution[4],
                1f to notificationDistribution[5]
            )
        } else {
            listOf(
                0f to 0.8f,
                0.2f to 0.4f,
                0.4f to 0.7f,
                0.6f to 0.2f,
                0.8f to 0.5f,
                1f to 0.3f
            )
        }
        
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
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Draw active glowing neon peak dot dynamically based on lowest Y (highest notifications)
        val peakPoint = points.minByOrNull { it.second } ?: points[3]
        val peakX = peakPoint.first * width
        val peakY = peakPoint.second * height
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = 6.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(peakX, peakY)
        )
        drawCircle(
            color = color,
            radius = 3.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(peakX, peakY)
        )
    }
}

@Composable
private fun UnlocksHourlyChart(
    unlockDistribution: List<Float>,
    modifier: Modifier = Modifier
) {
    val barHeights = unlockDistribution
    
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            barHeights.forEach { heightFraction ->
                val hasData = heightFraction > 0f
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(if (hasData) heightFraction.coerceIn(0.05f, 1f) else 0.05f)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .then(
                            if (hasData) {
                                Modifier.background(
                                    Brush.verticalGradient(
                                        listOf(AccentSecondary, AccentPrimary)
                                    )
                                )
                            } else Modifier.background(Color.White.copy(alpha = 0.04f))
                        )
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("12 AM", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
            Text("6 AM", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
            Text("12 PM", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
            Text("6 PM", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
            Text("12 AM", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
        }
    }
}
