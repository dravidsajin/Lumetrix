package com.lumetrix.statsmanager.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumetrix.statsmanager.core.time.DateUtils
import com.lumetrix.statsmanager.data.repository.UsageTrackingRepository
import com.lumetrix.statsmanager.data.tracking.UsageAccessChecker
import com.lumetrix.statsmanager.domain.model.DashboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: UsageTrackingRepository,
    private val usageAccessChecker: UsageAccessChecker,
) : ViewModel() {

    private val isSyncing = MutableStateFlow(false)

    /** Feature 1: Currently selected date for the Historical Day Browser. */
    private val selectedDate = MutableStateFlow(DateUtils.today())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = combine(
        selectedDate.flatMapLatest { date -> repository.observeDashboardState(date) },
        isSyncing,
    ) { dashboard, syncing ->
        dashboard.copy(isSyncing = syncing)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = DashboardUiState(isLoading = true),
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isSyncing.value = true
            runCatching { repository.syncRecentDays(dayCount = 7) }
            isSyncing.value = false
        }
    }

    fun onResume() {
        viewModelScope.launch {
            if (usageAccessChecker.hasUsageAccess()) {
                repository.syncToday()
            }
        }
    }

    fun openUsageAccessSettings(context: Context) {
        usageAccessChecker.launchUsageAccessSettings(context)
    }

    fun cycleAppCategory(packageName: String) {
        viewModelScope.launch {
            repository.cycleAppCategory(packageName)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Feature 1: Historical Day Browser
    // ─────────────────────────────────────────────────────────────

    fun selectDate(date: LocalDate) {
        // Don't allow future dates
        val today = DateUtils.today()
        selectedDate.value = if (date.isAfter(today)) today else date
        // Ensure data is synced for the selected date if in past
        if (date.isBefore(today)) {
            viewModelScope.launch {
                runCatching { repository.syncRecentDays(dayCount = 30) }
            }
        }
    }

    fun goToPreviousDay() {
        val current = selectedDate.value
        // Don't go back more than 30 days
        if (current.isAfter(DateUtils.today().minusDays(30))) {
            selectedDate.value = current.minusDays(1)
        }
    }

    fun goToNextDay() {
        val current = selectedDate.value
        val today = DateUtils.today()
        if (current.isBefore(today)) {
            selectedDate.value = current.plusDays(1)
        }
    }

    fun goToToday() {
        selectedDate.value = DateUtils.today()
    }
}
