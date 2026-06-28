package com.lumetrix.statsmanager.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumetrix.statsmanager.core.time.DateUtils
import com.lumetrix.statsmanager.data.local.dao.AppUsageDao
import com.lumetrix.statsmanager.domain.model.AppCategory
import com.lumetrix.statsmanager.domain.model.FocusState
import com.lumetrix.statsmanager.domain.model.FocusUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val appUsageDao: AppUsageDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusUiState())
    private var timerJob: Job? = null

    val uiState: StateFlow<FocusUiState> = combine(
        _uiState,
        appUsageDao.observeAllUsageBetween(DateUtils.toDayKey(DateUtils.today()), DateUtils.toDayKey(DateUtils.today()))
    ) { state, apps ->
        val distractingCount = apps.count { AppCategory.fromStorageKey(it.category) == AppCategory.Distracting }
        state.copy(distractingAppsCount = distractingCount)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FocusUiState()
    )

    fun selectDuration(minutes: Int) {
        if (_uiState.value.state != FocusState.Setup) return
        _uiState.value = _uiState.value.copy(
            selectedDurationMinutes = minutes,
            remainingTimeMillis = minutes * 60 * 1000L
        )
    }

    fun selectMode(mode: String) {
        if (_uiState.value.state != FocusState.Setup) return
        _uiState.value = _uiState.value.copy(selectedMode = mode)
    }

    fun startSession() {
        if (_uiState.value.state != FocusState.Setup) return
        
        _uiState.value = _uiState.value.copy(
            state = FocusState.Active,
            remainingTimeMillis = _uiState.value.selectedDurationMinutes * 60 * 1000L
        )
        
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingTimeMillis > 0) {
                delay(1000L)
                _uiState.value = _uiState.value.copy(
                    remainingTimeMillis = _uiState.value.remainingTimeMillis - 1000L
                )
            }
            // Timer finished
            _uiState.value = _uiState.value.copy(
                state = FocusState.Completed,
                remainingTimeMillis = 0L
            )
        }
    }

    fun endSession() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            state = FocusState.Setup,
            remainingTimeMillis = _uiState.value.selectedDurationMinutes * 60 * 1000L
        )
    }
}
