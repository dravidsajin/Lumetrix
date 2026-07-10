package com.lumetrix.statsmanager.domain.model

data class AppsUiState(
    val isLoading: Boolean = true,
    val hasUsageAccess: Boolean = false,
    val allApps: List<AppUsageItem> = emptyList(),
    val displayApps: List<AppUsageItem> = emptyList(),
    val selectedCategory: AppCategory? = null,
    val totalTimeLabel: String = "0m"
)
