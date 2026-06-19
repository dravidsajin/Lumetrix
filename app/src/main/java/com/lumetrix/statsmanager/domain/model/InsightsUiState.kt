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
)
