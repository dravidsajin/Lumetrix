package com.lumetrix.statsmanager.ui.appdetails

import android.widget.Toast
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumetrix.statsmanager.domain.model.AppCategory
import com.lumetrix.statsmanager.ui.components.AmbientBackground
import com.lumetrix.statsmanager.ui.components.GlassCard
import com.lumetrix.statsmanager.ui.components.GradientGlassCard
import com.lumetrix.statsmanager.ui.components.NeonLineChart
import com.lumetrix.statsmanager.ui.components.PremiumPillButton
import com.lumetrix.statsmanager.ui.components.StatCard
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.Warning
import com.lumetrix.statsmanager.ui.theme.Danger

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

    androidx.compose.runtime.LaunchedEffect(packageName) {
        viewModel.loadAppDetails(packageName)
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
                                        com.lumetrix.statsmanager.ui.theme.AccentSecondary.copy(alpha = 0.25f),
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
                    GradientGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = uiState.appName,
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary,
                            )
                            Text(
                                text = uiState.category.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = uiState.categoryColor,
                            )
                        }
                    }

                    // Expanded Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(LumetrixTokens.CardSpacing),
                    ) {
                        StatCard(
                            label = "Usage Today",
                            value = uiState.todayDurationLabel,
                            modifier = Modifier.weight(1f),
                            accent = AccentPrimary,
                        )
                        StatCard(
                            label = "Sessions",
                            value = "${uiState.todaySessionCount}",
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            label = "Avg Session",
                            value = uiState.averageSessionLabel,
                            modifier = Modifier.weight(1f),
                            accent = Warning,
                        )
                    }

                    // Category Selector
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Category",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AppCategory.entries.forEach { cat ->
                                    val isSelected = uiState.category == cat
                                    val catColor = when (cat) {
                                        AppCategory.Productive -> Success
                                        AppCategory.Neutral -> Warning
                                        AppCategory.Distracting -> Danger
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) catColor.copy(alpha = 0.2f) else Color.Transparent
                                            )
                                            .clickable { viewModel.updateCategory(cat) }
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

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Weekly Trend",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                            )
                            NeonLineChart(
                                data = uiState.weeklyUsageChart.ifEmpty {
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
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    PremiumPillButton(
                        text = "Add to Focus Blocklist (Coming Soon)",
                        onClick = { 
                            Toast.makeText(context, "Focus modes coming soon!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
