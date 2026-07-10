package com.lumetrix.statsmanager.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumetrix.statsmanager.data.repository.UsageTrackingRepository
import com.lumetrix.statsmanager.data.tracking.UsageAccessChecker
import com.lumetrix.statsmanager.domain.model.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UsageTrackingRepository,
    private val usageAccessChecker: UsageAccessChecker,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = repository.observeProfileState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = ProfileUiState(isLoading = true),
        )

    init {
        viewModelScope.launch {
            repository.syncRecentDays(dayCount = 7)
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
}
