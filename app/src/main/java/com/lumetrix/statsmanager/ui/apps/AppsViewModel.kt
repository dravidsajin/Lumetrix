package com.lumetrix.statsmanager.ui.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumetrix.statsmanager.core.time.DateUtils
import com.lumetrix.statsmanager.core.time.DurationFormatter
import com.lumetrix.statsmanager.data.local.dao.AppUsageDao
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
    private val dashboardMapper: DashboardMapper,
    private val usageAccessChecker: UsageAccessChecker,
) : ViewModel() {

    private val selectedCategory = MutableStateFlow<AppCategory?>(null)

    val uiState: StateFlow<AppsUiState> = combine(
        appUsageDao.observeAllUsageBetween(DateUtils.toDayKey(DateUtils.today()), DateUtils.toDayKey(DateUtils.today())),
        selectedCategory
    ) { apps, category ->
        val hasAccess = usageAccessChecker.hasUsageAccess()
        val allAppItems = dashboardMapper.toAppUsageItems(apps).sortedByDescending { it.durationMs }
        val displayApps = if (category == null) allAppItems else allAppItems.filter { it.category == category }
        
        val totalMs = displayApps.sumOf { it.durationMs }
        val totalTimeLabel = DurationFormatter.formatShort(totalMs)
        
        AppsUiState(
            isLoading = false,
            hasUsageAccess = hasAccess,
            allApps = allAppItems,
            displayApps = displayApps,
            selectedCategory = category,
            totalTimeLabel = totalTimeLabel
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
