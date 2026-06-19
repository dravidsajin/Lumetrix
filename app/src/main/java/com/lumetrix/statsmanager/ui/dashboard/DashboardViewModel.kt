package com.lumetrix.statsmanager.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumetrix.statsmanager.data.repository.UsageTrackingRepository
import com.lumetrix.statsmanager.data.tracking.UsageAccessChecker
import com.lumetrix.statsmanager.domain.model.DashboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: UsageTrackingRepository,
    private val usageAccessChecker: UsageAccessChecker,
) : ViewModel() {

    private val isSyncing = MutableStateFlow(false)

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeDashboardState(),
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
}
