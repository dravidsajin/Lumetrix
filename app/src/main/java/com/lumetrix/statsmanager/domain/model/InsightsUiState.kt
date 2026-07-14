package com.lumetrix.statsmanager.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class BehavioralInsightItem(
    val title: String,
    val subtitle: String,
    val iconKey: InsightIcon,
)

data class RecommendationItem(
    val title: String,
    val body: String,
)

enum class InsightIcon {
    Night,
    Bedtime,
    Trending,
    Psychology,
}

data class InsightsUiState(
    val hasUsageAccess: Boolean = false,
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val weeklyScreenTimeHours: List<Float> = emptyList(),
    val productivePercent: Int = 0,
    val neutralPercent: Int = 0,
    val distractingPercent: Int = 0,
    val behavioralInsights: List<BehavioralInsightItem> = emptyList(),
    val recommendations: List<RecommendationItem> = emptyList(),
    val lastSyncedLabel: String? = null,

    // Feature 4: Distraction Index
    val distractionIndex: Int = 0,
    val distractionIndexLabel: String = "—",

    // Feature 7: Pomodoro / Focus Session Stats
    val weeklyFocusSessions: Int = 0,
    val focusSuccessRate: Int = 0,
    val avgFocusSessionMin: Int = 0,
    val recentFocusSessions: List<FocusSessionItem> = emptyList(),

    // --- Insights Revamp Extras ---
    val selectedTab: Int = 0,
    val screenTimeChangePercent: Int = 0,
    val habitScoreChangePercent: Int = 0,
    val focusPointsEarned: Int = 0,
    val periodUsage: PeriodUsage = PeriodUsage(),
    val todayPeriodUsage: PeriodUsage = PeriodUsage(),
    val focusHeatmap: List<FocusHeatmapPoint> = emptyList(),
    val doomscrollApps: List<DoomscrollAppItem> = emptyList(),
)

data class FocusHeatmapPoint(
    val dayKey: Int,
    val dayLabel: String, // e.g. "Jul 10"
    val dateLabel: String, // e.g. "Jul 10"
    val focusScore: Int, // 0 to 100 focus score
)

data class DoomscrollAppItem(
    val packageName: String,
    val appName: String,
    val avgSessionMin: Int,
    val totalSessions: Int,
)

data class PeriodUsage(
    val morningMs: Long = 0L,
    val afternoonMs: Long = 0L,
    val eveningMs: Long = 0L,
    val nightMs: Long = 0L,
)


data class AchievementItem(
    val title: String,
    val subtitle: String,
    val unlocked: Boolean,
)

data class ProfileUiState(
    val hasUsageAccess: Boolean = false,
    val isLoading: Boolean = true,
    val userName: String = "You",
    val productivityTitle: String = "Getting Started",
    val level: Int = 1,
    val achievements: List<AchievementItem> = emptyList(),
    val focusHoursLabel: String = "0h",
    val daysTrackedLabel: String = "0",
    val distractionReductionLabel: String = "—",
    val averageFocusScore: Int = 0,
    val focusPointsBalance: Int = 0,
    val levelProgress: Float = 0f,
    val dailyScreenTimeTargetHours: Int = 4,
    val dailyFocusScoreTarget: Int = 70,
    val maxPickupsTarget: Int = 30,
)
