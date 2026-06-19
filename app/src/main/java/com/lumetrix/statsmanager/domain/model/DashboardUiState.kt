package com.lumetrix.statsmanager.domain.model

import androidx.compose.ui.graphics.Color
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.Warning

enum class AppCategory(val label: String, val storageKey: String) {
    Productive("Productive", "productive"),
    Neutral("Neutral", "neutral"),
    Distracting("Distracting", "distracting"),
    ;

    companion object {
        fun fromStorageKey(key: String): AppCategory =
            entries.firstOrNull { it.storageKey == key } ?: Neutral
    }
}

data class AppUsageItem(
    val packageName: String,
    val appName: String,
    val durationMs: Long,
    val durationLabel: String,
    val category: AppCategory,
    val categoryColor: Color,
)

data class DashboardUiState(
    val greeting: String = "",
    val userName: String = "You",
    val hasUsageAccess: Boolean = false,
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val focusScore: Int = 0,
    val insightTitle: String = "Sync your usage data",
    val insightSubtitle: String = "Grant usage access to unlock real analytics.",
    val weeklyScreenTimeHours: List<Float> = emptyList(),
    val topApps: List<AppUsageItem> = emptyList(),
    val unlockCount: Int = 0,
    val notificationCount: Int = 0,
    val pickupCount: Int = 0,
    val focusTimeLabel: String = "0m",
    val totalScreenTimeLabel: String = "0m",
    val lastSyncedLabel: String? = null,
    val syncError: String? = null,
)
