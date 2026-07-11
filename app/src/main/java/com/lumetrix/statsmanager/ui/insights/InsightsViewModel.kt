package com.lumetrix.statsmanager.ui.insights

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumetrix.statsmanager.data.repository.UsageTrackingRepository
import com.lumetrix.statsmanager.data.tracking.UsageAccessChecker
import com.lumetrix.statsmanager.domain.model.InsightsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val repository: UsageTrackingRepository,
    private val usageAccessChecker: UsageAccessChecker,
) : ViewModel() {

    private val isSyncing = MutableStateFlow(false)
    private val selectedTab = MutableStateFlow(0)

    val uiState: StateFlow<InsightsUiState> = combine(
        repository.observeInsightsState(),
        isSyncing,
        selectedTab,
    ) { insights, syncing, tab ->
        insights.copy(
            isSyncing = syncing,
            selectedTab = tab,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = InsightsUiState(isLoading = true),
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isSyncing.value = true
            repository.syncRecentDays(dayCount = 7)
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

    fun selectTab(index: Int) {
        selectedTab.value = index
    }
}
