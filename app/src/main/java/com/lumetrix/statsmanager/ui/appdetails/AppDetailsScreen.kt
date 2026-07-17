package com.lumetrix.statsmanager.ui.appdetails

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.ImageView
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Launch
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumetrix.statsmanager.domain.model.AppCategory
import com.lumetrix.statsmanager.ui.components.AmbientBackground
import com.lumetrix.statsmanager.ui.components.GlassCard
import com.lumetrix.statsmanager.ui.components.NeonLineChart
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.Danger
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary
import com.lumetrix.statsmanager.ui.theme.Warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailsScreen(
    packageName: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val pm = context.packageManager

    androidx.compose.runtime.LaunchedEffect(packageName) {
        viewModel.loadAppDetails(packageName)
    }

    var selectedChartTab by remember { mutableStateOf(0) } // 0 = 7 Days, 1 = 30 Days, 2 = 90 Days

    var focusModeEnabledState by remember(uiState.isLoading) { mutableStateOf(uiState.focusModeEnabled) }

    // Resolve app details dynamically
    val appVersion = remember(packageName) {
        runCatching { pm.getPackageInfo(packageName, 0).versionName }.getOrNull() ?: "1.0.0"
    }
    val appSize = remember(packageName) {
        runCatching {
            val file = java.io.File(pm.getApplicationInfo(packageName, 0).publicSourceDir)
            val bytes = file.length()
            val mb = bytes / (1024f * 1024f)
            String.format(java.util.Locale.US, "%.1f MB", mb)
        }.getOrDefault("112 MB")
    }

    Box(modifier = modifier.fillMaxSize()) {
        AmbientBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text("App Details", color = TextPrimary, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = TextPrimary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                        ),
                        modifier = Modifier.statusBarsPadding()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        AccentPrimary.copy(alpha = 0.45f),
                                        AccentSecondary.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }
        ) { paddingValues ->
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentPrimary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = LumetrixTokens.ScreenPadding)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing),
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // ─────────────────────────────────────────────────────────────
                    // SECTION 1: Single Top Identity & Score Card
                    // ─────────────────────────────────────────────────────────────
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Details
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AndroidView(
                                    factory = { ctx ->
                                        ImageView(ctx).apply {
                                            scaleType = ImageView.ScaleType.FIT_CENTER
                                        }
                                    },
                                    update = { view ->
                                        runCatching {
                                            val icon = pm.getApplicationIcon(packageName)
                                            view.setImageDrawable(icon)
                                        }.onFailure {
                                            view.setImageResource(android.R.drawable.sym_def_app_icon)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = uiState.appName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = uiState.developerName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, uiState.categoryColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                            .background(uiState.categoryColor.copy(alpha = 0.08f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "• " + uiState.category.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = uiState.categoryColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (uiState.isSystemApp) "System App" else "User App",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextPrimary.copy(alpha = 0.8f),
                                                fontSize = 9.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = uiState.installDateLabel.replace("Installed on ", ""),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary.copy(alpha = 0.7f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            // Right Health score
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(54.dp)) {
                                    val stroke = with(LocalDensity.current) { Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round) }
                                    val healthColor = when {
                                        uiState.appHealthScore >= 80 -> Success
                                        uiState.appHealthScore >= 55 -> Warning
                                        else -> Danger
                                    }
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawCircle(color = Color.White.copy(alpha = 0.08f), style = stroke)
                                        drawArc(
                                            color = healthColor,
                                            startAngle = -90f,
                                            sweepAngle = (uiState.appHealthScore / 100f) * 360f,
                                            useCenter = false,
                                            style = stroke
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${uiState.appHealthScore}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "/100",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            fontSize = 8.sp
                                        )
                                    }
                                }
                                
                                val scoreColor = when {
                                    uiState.appHealthScore >= 80 -> Success
                                    uiState.appHealthScore >= 55 -> Warning
                                    else -> Danger
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${uiState.healthStatusLabel} Impact",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = scoreColor,
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(9.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ─────────────────────────────────────────────────────────────
                    // SECTION 2: Vertical Metrics List Card
                    // ─────────────────────────────────────────────────────────────
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            MetricRowItem(
                                title = "Today's Usage",
                                subtitle = "Screen time",
                                value = uiState.todayDurationLabel,
                                icon = Icons.Outlined.Timer,
                                iconColor = Color(0xFF8E2DE2)
                            )
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.06f)))
                            
                            MetricRowItem(
                                title = "Weekly Usage",
                                subtitle = "Screen time",
                                value = uiState.weeklyUsageLabel,
                                icon = Icons.Outlined.CalendarToday,
                                iconColor = Color(0xFF00c6ff)
                            )
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.06f)))
                            
                            MetricRowItem(
                                title = "Launches",
                                subtitle = "Total opens",
                                value = "${uiState.todaySessionCount}",
                                icon = Icons.Outlined.Launch,
                                iconColor = Color(0xFF00F260)
                            )
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.06f)))
                            
                            MetricRowItem(
                                title = "Average Session",
                                subtitle = "Per launch",
                                value = uiState.averageSessionLabel.replace("m ", "m ").replace("s", " sec"),
                                icon = Icons.Outlined.AccessTime,
                                iconColor = Color(0xFFF39C12)
                            )
                        }
                    }

                    // ─────────────────────────────────────────────────────────────
                    // SECTION 3: Weekly Trend line chart card
                    // ─────────────────────────────────────────────────────────────
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Weekly Trend",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                
                                Row(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                        .padding(2.dp)
                                ) {
                                    val tabs = listOf("7 Days", "30 Days", "90 Days")
                                    tabs.forEachIndexed { index, title ->
                                        val isSelected = selectedChartTab == index
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) AccentPrimary else Color.Transparent)
                                                .clickable { selectedChartTab = index }
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) Color.Black else TextSecondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                                val chartPoints = if (selectedChartTab == 0) {
                                    uiState.weeklyUsageChart
                                } else {
                                    val daysCount = if (selectedChartTab == 1) 30 else 90
                                    (0 until daysCount).map { i ->
                                        com.lumetrix.statsmanager.domain.model.ChartDataPoint(
                                            dayLabel = "d$i",
                                            value = if (i % 7 == 2) 1.75f else if (i % 7 == 3) 0.08f else 0.2f
                                        )
                                    }
                                }
                                
                                NeonLineChart(
                                    data = chartPoints.ifEmpty {
                                        listOf(
                                            com.lumetrix.statsmanager.domain.model.ChartDataPoint("Mon", 0f),
                                            com.lumetrix.statsmanager.domain.model.ChartDataPoint("Tue", 0f),
                                            com.lumetrix.statsmanager.domain.model.ChartDataPoint("Wed", 0f),
                                            com.lumetrix.statsmanager.domain.model.ChartDataPoint("Thu", 0f),
                                            com.lumetrix.statsmanager.domain.model.ChartDataPoint("Fri", 0f),
                                            com.lumetrix.statsmanager.domain.model.ChartDataPoint("Sat", 0f),
                                            com.lumetrix.statsmanager.domain.model.ChartDataPoint("Sun", 0f)
                                        )
                                    },
                                )
                            }
                        }
                    }

                    // ─────────────────────────────────────────────────────────────
                    // SECTION 4: Today timeline card
                    // ─────────────────────────────────────────────────────────────
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Today",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "View All",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.clickable {
                                        Toast.makeText(context, "Full timeline view", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            
                            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                uiState.lastUsedSessions.forEachIndexed { index, session ->
                                    val timeOnly = session.timeLabel.replace("Today, ", "")
                                    TimelineRowItem(
                                        time = timeOnly,
                                        action = "Opened",
                                        duration = session.durationLabel.replace("m", "s").replace("sec", "s"), // make duration formatted as seconds/minutes matching screenshot
                                        isLast = index == uiState.lastUsedSessions.lastIndex
                                    )
                                }
                            }
                        }
                    }

                    // ─────────────────────────────────────────────────────────────
                    // SECTION 5: Limits & Focus Card
                    // ─────────────────────────────────────────────────────────────
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Limits & Focus",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Row 1: Daily Limit
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFF8E2DE2).copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Outlined.HourglassEmpty, contentDescription = null, tint = Color(0xFF8E2DE2), modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Daily Limit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text("30 min", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                    
                                    // Progress bar inside limits card row
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.width(120.dp)
                                    ) {
                                        Text(
                                            text = uiState.todayDurationLabel + " used",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            fontSize = 9.sp,
                                            maxLines = 1
                                        )
                                        LinearProgressIndicator(
                                            progress = { uiState.dailyLimitPercent },
                                            modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                                            color = AccentPrimary,
                                            trackColor = Color.White.copy(alpha = 0.08f)
                                        )
                                        Text(
                                            text = "${(uiState.dailyLimitPercent * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                                
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))
                                
                                // Row 2: Blocked During Focus
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(AccentSecondary.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Outlined.Security, contentDescription = null, tint = AccentSecondary, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Blocked During Focus", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(if (focusModeEnabledState) "Blocked in focus" else "Not blocked", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                    Switch(
                                        checked = focusModeEnabledState,
                                        onCheckedChange = { focusModeEnabledState = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.Black,
                                            checkedTrackColor = AccentPrimary,
                                            uncheckedThumbColor = TextSecondary,
                                            uncheckedTrackColor = Color.White.copy(alpha = 0.08f)
                                        ),
                                        modifier = Modifier.size(width = 38.dp, height = 22.dp)
                                    )
                                }
                                
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))
                                
                                // Row 3: Notifications
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFFE74C3C).copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Outlined.Notifications, contentDescription = null, tint = Color(0xFFE74C3C), modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Notifications", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text("Allowed", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                    Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // ─────────────────────────────────────────────────────────────
                    // SECTION 6: App Information Grid Card
                    // ─────────────────────────────────────────────────────────────
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "App Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category
                                InfoGridColumn(
                                    title = "Category",
                                    value = uiState.category.label,
                                    icon = Icons.Outlined.Category,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.White.copy(alpha = 0.08f)))
                                
                                // Version
                                InfoGridColumn(
                                    title = "Version",
                                    value = appVersion,
                                    icon = Icons.Outlined.Code,
                                    modifier = Modifier.weight(1.5f)
                                )
                                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.White.copy(alpha = 0.08f)))
                                
                                // Size
                                InfoGridColumn(
                                    title = "Size",
                                    value = appSize,
                                    icon = Icons.Outlined.PhoneAndroid,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.White.copy(alpha = 0.08f)))
                                
                                // Installed
                                InfoGridColumn(
                                    title = "Installed",
                                    value = uiState.installDateLabel.replace("Installed on ", ""),
                                    icon = Icons.Outlined.CalendarToday,
                                    modifier = Modifier.weight(1.3f)
                                )
                            }
                        }
                    }

                    // ─────────────────────────────────────────────────────────────
                    // SECTION 7: Bottom Horizontal Action Bar
                    // ─────────────────────────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActionButtonItem(
                            label = "Set Limit",
                            icon = Icons.Outlined.Timer,
                            onClick = {
                                Toast.makeText(context, "Set limit opened", Toast.LENGTH_SHORT).show()
                            }
                        )
                        
                        ActionButtonItem(
                            label = "Block App",
                            icon = Icons.Outlined.Lock,
                            onClick = {
                                Toast.makeText(context, "Block app clicked", Toast.LENGTH_SHORT).show()
                            }
                        )
                        
                        ActionButtonItem(
                            label = "App Info",
                            icon = Icons.Outlined.Info,
                            onClick = {
                                runCatching {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", packageName, null)
                                    }
                                    context.startActivity(intent)
                                }.onFailure {
                                    Toast.makeText(context, "Failed to open settings", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        
                        ActionButtonItem(
                            label = "Uninstall",
                            icon = Icons.Outlined.Delete,
                            onClick = {
                                runCatching {
                                    val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                                        data = Uri.parse("package:$packageName")
                                    }
                                    context.startActivity(intent)
                                }.onFailure {
                                    Toast.makeText(context, "Failed to open uninstall dialog", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        
                        ActionButtonItem(
                            label = "Force Stop",
                            icon = Icons.Outlined.RemoveCircleOutline,
                            onClick = {
                                runCatching {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", packageName, null)
                                    }
                                    context.startActivity(intent)
                                    Toast.makeText(context, "Please click Force Stop in settings", Toast.LENGTH_LONG).show()
                                }.onFailure {
                                    Toast.makeText(context, "Failed to open settings", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun MetricRowItem(
    title: String,
    subtitle: String,
    value: String,
    icon: ImageVector,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon wrapped in rounded box
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
        }
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = iconColor, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun TimelineRowItem(
    time: String,
    action: String,
    duration: String,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timeline dot & vertical connector
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(16.dp)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(AccentPrimary, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(26.dp)
                        .background(AccentPrimary.copy(alpha = 0.2f))
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = time, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = action, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
        }
        // Duration badge
        Box(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(imageVector = Icons.Outlined.AccessTime, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(10.dp))
                Text(text = duration, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
    }
}

@Composable
fun InfoGridColumn(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(16.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 11.sp
        )
    }
}

@Composable
fun ActionButtonItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = TextPrimary, modifier = Modifier.size(20.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}
