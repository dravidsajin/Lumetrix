package com.lumetrix.statsmanager.ui.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumetrix.statsmanager.core.time.DateUtils
import com.lumetrix.statsmanager.core.time.DurationFormatter
import com.lumetrix.statsmanager.data.local.dao.AppUsageDao
import com.lumetrix.statsmanager.data.local.dao.DailySummaryDao
import com.lumetrix.statsmanager.data.local.dao.UnlockEventDao
import com.lumetrix.statsmanager.data.tracking.UsageAccessChecker
import com.lumetrix.statsmanager.domain.mapper.DashboardMapper
import com.lumetrix.statsmanager.domain.model.AppCategory
import com.lumetrix.statsmanager.domain.model.AppsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppsViewModel @Inject constructor(
    private val appUsageDao: AppUsageDao,
    private val dailySummaryDao: DailySummaryDao,
    private val unlockEventDao: UnlockEventDao,
    private val dashboardMapper: DashboardMapper,
    private val usageAccessChecker: UsageAccessChecker,
) : ViewModel() {

    private val selectedCategory = MutableStateFlow<AppCategory?>(null)

    val uiState: StateFlow<AppsUiState> = combine(
        appUsageDao.observeAllUsageBetween(DateUtils.toDayKey(DateUtils.today()), DateUtils.toDayKey(DateUtils.today())),
        dailySummaryDao.observeSummary(DateUtils.toDayKey(DateUtils.today())),
        unlockEventDao.observeUnlockEventsForDate(DateUtils.toDayKey(DateUtils.today())),
        selectedCategory
    ) { apps, summary, unlockEvents, category ->
        val hasAccess = usageAccessChecker.hasUsageAccess()
        val allAppItems = dashboardMapper.toAppUsageItems(apps).sortedByDescending { it.durationMs }
        val displayApps = if (category == null) allAppItems else allAppItems.filter { it.category == category }
        
        val totalMs = displayApps.sumOf { it.durationMs }
        val totalTimeLabel = DurationFormatter.formatShort(totalMs)
        
        val unlocks = summary?.unlockCount ?: 0
        val notifications = summary?.notificationCount ?: 0

        // 1. Calculate Unlocks hourly distribution (24 bins)
        val hourlyUnlocks = IntArray(24) { 0 }
        unlockEvents.forEach { event ->
            val localDateTime = java.time.Instant.ofEpochMilli(event.timestampMs)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime()
            val hr = localDateTime.hour
            if (hr in 0..23) {
                hourlyUnlocks[hr]++
            }
        }

        val maxUnlock = hourlyUnlocks.maxOrNull()?.coerceAtLeast(1) ?: 1
        val unlockDistribution = if (unlocks > 0) {
            hourlyUnlocks.map { it.toFloat() / maxUnlock }
        } else {
            List(24) { 0f }
        }

        // 2. Calculate Notifications wave distribution (6 points for 4-hour intervals)
        val blockUnlocks = FloatArray(6) { 0f }
        for (i in 0..23) {
            val block = i / 4
            blockUnlocks[block] += hourlyUnlocks[i].toFloat()
        }

        val maxBlock = blockUnlocks.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val notificationDistribution = if (notifications > 0) {
            blockUnlocks.map { 1f - (0.15f + (it / maxBlock) * 0.7f) }
        } else {
            List(6) { 1f }
        }
        
        AppsUiState(
            isLoading = false,
            hasUsageAccess = hasAccess,
            allApps = allAppItems,
            displayApps = displayApps,
            selectedCategory = category,
            totalTimeLabel = totalTimeLabel,
            unlockCount = unlocks,
            notificationCount = notifications,
            unlockDistribution = unlockDistribution,
            notificationDistribution = notificationDistribution
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppsUiState(isLoading = true)
    )

    fun selectCategory(category: AppCategory?) {
        selectedCategory.value = category
    }
}
