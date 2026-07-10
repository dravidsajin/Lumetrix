package com.lumetrix.statsmanager.domain.model

import androidx.compose.ui.graphics.Color
import com.lumetrix.statsmanager.ui.theme.Warning

data class AppDetailsUiState(
    val isLoading: Boolean = true,
    val packageName: String = "",
    val appName: String = "",
    val category: AppCategory = AppCategory.Neutral,
    val categoryColor: Color = Warning,
    val todayDurationLabel: String = "0m",
    val todaySessionCount: Int = 0,
    val averageSessionLabel: String = "0m",
    val weeklyUsageChart: List<ChartDataPoint> = emptyList(),
)
