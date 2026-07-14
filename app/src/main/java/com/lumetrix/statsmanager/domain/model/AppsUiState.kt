package com.lumetrix.statsmanager.domain.model

data class AppsUiState(
    val isLoading: Boolean = true,
    val hasUsageAccess: Boolean = false,
    val allApps: List<AppUsageItem> = emptyList(),
    val displayApps: List<AppUsageItem> = emptyList(),
    val selectedCategory: AppCategory? = null,
    val totalTimeLabel: String = "0m",
    val unlockCount: Int = 0,
    val notificationCount: Int = 0,
    val unlockDistribution: List<Float> = emptyList(),
    val notificationDistribution: List<Float> = emptyList()
)
