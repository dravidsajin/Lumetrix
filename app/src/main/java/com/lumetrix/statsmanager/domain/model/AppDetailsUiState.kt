package com.lumetrix.statsmanager.domain.model

import androidx.compose.ui.graphics.Color
import com.lumetrix.statsmanager.ui.theme.Warning

data class LastUsedSessionItem(
    val timeLabel: String,
    val durationLabel: String,
)

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

    // Screenshot 2 details
    val developerName: String = "",
    val installDateLabel: String = "",
    val isSystemApp: Boolean = false,
    val appLabelTag: String = "",
    val appHealthScore: Int = 0,
    val healthStatusLabel: String = "",
    val healthStatusDesc: String = "",

    // Compare metrics
    val todayUsageChangeLabel: String = "",
    val todayUsageIsPositive: Boolean = false, // true = red/increase, false = green/decrease
    val weeklyUsageLabel: String = "0m",
    val weeklyUsageChangeLabel: String = "",
    val weeklyUsageIsPositive: Boolean = false,
    val todayLaunches: Int = 0,
    val todayLaunchesChangeLabel: String = "",
    val todayLaunchesIsPositive: Boolean = false,
    val averageSessionChangeLabel: String = "",
    val averageSessionIsPositive: Boolean = false,

    // AI Insights
    val aiInsights: List<String> = emptyList(),

    // Category & Impact
    val impactLevelLabel: String = "",
    val aiConfidencePercent: Int = 0,
    val mainImpactLabel: String = "",

    // Limits & Rules
    val dailyLimitLabel: String = "Not set",
    val dailyLimitPercent: Float = 0f,
    val focusModeEnabled: Boolean = false,
    val scheduleEnabled: Boolean = false,
    val scheduleTimeLabel: String = "9:00 PM – 8:00 AM",
    val appTimerEnabled: Boolean = false,

    // Background usage & timeline
    val backgroundUsageLabel: String = "0m",
    val lastUsedSessions: List<LastUsedSessionItem> = emptyList(),
)
