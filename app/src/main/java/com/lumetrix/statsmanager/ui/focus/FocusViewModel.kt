package com.lumetrix.statsmanager.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumetrix.statsmanager.core.time.DateUtils
import com.lumetrix.statsmanager.data.local.dao.AppUsageDao
import com.lumetrix.statsmanager.data.local.entity.AppChainRuleEntity
import com.lumetrix.statsmanager.data.repository.UsageTrackingRepository
import com.lumetrix.statsmanager.domain.evaluator.AppChainEvaluator
import com.lumetrix.statsmanager.domain.model.AppCategory
import com.lumetrix.statsmanager.domain.model.FocusSessionItem
import com.lumetrix.statsmanager.domain.model.FocusState
import com.lumetrix.statsmanager.domain.model.FocusUiState
import com.lumetrix.statsmanager.domain.model.SimpleAppInfo
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
    private val repository: UsageTrackingRepository,
    private val appChainEvaluator: AppChainEvaluator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusUiState())
    private var timerJob: Job? = null
    private var sessionStartMs: Long = 0L

    val uiState: StateFlow<FocusUiState> = combine(
        _uiState,
        appUsageDao.observeAllUsageBetween(
            DateUtils.toDayKey(DateUtils.today()),
            DateUtils.toDayKey(DateUtils.today())
        ),
        repository.observeFocusPoints(),
        repository.observeChainRules(),
        repository.observeWeeklyFocusSessions(),
    ) { state, apps, points, chainRuleEntities, weekSessions ->
        val today = DateUtils.today()
        val todayKey = DateUtils.toDayKey(today)

        val distractingCount = apps.count { AppCategory.fromStorageKey(it.category) == AppCategory.Distracting }
        val enrichedRules = try {
            appChainEvaluator.enrichRulesWithProgress(chainRuleEntities)
        } catch (e: Exception) {
            emptyList()
        }

        // Map raw DB sessions → FocusSessionItem presentation models
        val allSessionItems: List<FocusSessionItem> = weekSessions
            .sortedByDescending { it.startTimeMs }
            .map { entity ->
                val sessionDate = DateUtils.fromDayKey(entity.sessionDate)
                val dateLabel = when {
                    sessionDate == today -> "Today"
                    sessionDate == today.minusDays(1) -> "Yesterday"
                    else -> sessionDate.dayOfWeek
                        .getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()) +
                            " " + sessionDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM d"))
                }
                val timeLabel = java.time.Instant.ofEpochMilli(entity.startTimeMs)
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))
                val actualMin = ((entity.endTimeMs - entity.startTimeMs) / 60_000L).toInt()
                    .coerceAtLeast(0)
                FocusSessionItem(
                    id = entity.id,
                    mode = entity.mode,
                    plannedDurationMin = entity.plannedDurationMin,
                    actualDurationMin = actualMin,
                    wasCompleted = entity.wasCompleted,
                    pointsEarned = entity.pointsEarned,
                    dateLabel = dateLabel,
                    timeLabel = timeLabel,
                )
            }

        // Today's stats
        val todaySessions = weekSessions.filter { it.sessionDate == todayKey }
        val todayCompleted = todaySessions.count { it.wasCompleted }
        val todayFocusMin = todaySessions.sumOf { entity ->
            ((entity.endTimeMs - entity.startTimeMs) / 60_000L).toInt().coerceAtLeast(0)
        }
        val todaySuccessRate = if (todaySessions.isNotEmpty()) {
            (todayCompleted * 100) / todaySessions.size
        } else 0

        // Weekly stats
        val completedThisWeek = weekSessions.count { it.wasCompleted }
        val successRate = if (weekSessions.isNotEmpty()) {
            completedThisWeek * 100 / weekSessions.size
        } else 0

        state.copy(
            distractingAppsCount = distractingCount,
            focusPointsBalance = points,
            chainRules = enrichedRules,
            weeklyCompletedSessions = completedThisWeek,
            weeklySuccessRate = successRate,
            recentSessions = allSessionItems,
            todayFocusMinutes = todayFocusMin,
            todaySessionCount = todaySessions.size,
            todaySuccessRate = todaySuccessRate,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FocusUiState()
    )

    init {
        viewModelScope.launch {
            val apps = repository.getInstalledApps()
            _uiState.value = _uiState.value.copy(availableApps = apps)
        }
    }

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

        sessionStartMs = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(
            state = FocusState.Active,
            remainingTimeMillis = _uiState.value.selectedDurationMinutes * 60 * 1000L,
            pointsJustEarned = 0,
        )
        startTimer()
    }

    fun pauseSession() {
        if (_uiState.value.state != FocusState.Active) return
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(state = FocusState.Paused)
    }

    fun resumeSession() {
        if (_uiState.value.state != FocusState.Paused) return
        _uiState.value = _uiState.value.copy(state = FocusState.Active)
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingTimeMillis > 0 && _uiState.value.state == FocusState.Active) {
                delay(1000L)
                if (_uiState.value.state == FocusState.Active) {
                    _uiState.value = _uiState.value.copy(
                        remainingTimeMillis = _uiState.value.remainingTimeMillis - 1000L
                    )
                }
            }
            if (_uiState.value.remainingTimeMillis <= 0L && _uiState.value.state == FocusState.Active) {
                onSessionFinished(wasCompleted = true)
            }
        }
    }

    fun endSession() {
        timerJob?.cancel()
        val state = _uiState.value.state
        if (state == FocusState.Active || state == FocusState.Paused) {
            onSessionFinished(wasCompleted = false)
        } else {
            _uiState.value = _uiState.value.copy(
                state = FocusState.Setup,
                remainingTimeMillis = _uiState.value.selectedDurationMinutes * 60 * 1000L,
                pointsJustEarned = 0,
            )
        }
    }

    private fun onSessionFinished(wasCompleted: Boolean) {
        val endMs = System.currentTimeMillis()
        val state = _uiState.value
        val pointsEarned = if (wasCompleted) (state.selectedDurationMinutes * 2) else (state.selectedDurationMinutes / 4)

        viewModelScope.launch {
            repository.recordFocusSession(
                startTimeMs = sessionStartMs,
                endTimeMs = endMs,
                mode = state.selectedMode,
                plannedDurationMin = state.selectedDurationMinutes,
                wasCompleted = wasCompleted,
            )
        }

        _uiState.value = state.copy(
            state = FocusState.Completed,
            remainingTimeMillis = 0L,
            pointsJustEarned = pointsEarned,
        )
    }

    fun dismissCompleted() {
        _uiState.value = _uiState.value.copy(
            state = FocusState.Setup,
            remainingTimeMillis = _uiState.value.selectedDurationMinutes * 60 * 1000L,
            pointsJustEarned = 0,
        )
    }

    // ─────────────────────────────────────────────────────────────
    // Feature 6: App Chain Rules
    // ─────────────────────────────────────────────────────────────

    fun setAddRuleDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showAddRuleDialog = visible)
    }

    fun addChainRule(
        gatePackage: String,
        gateAppName: String,
        gateDurationMin: Int,
        targetPackage: String,
        targetAppName: String,
    ) {
        viewModelScope.launch {
            repository.addChainRule(
                gatePackage = gatePackage,
                gateAppName = gateAppName,
                gateDurationMin = gateDurationMin,
                targetPackage = targetPackage,
                targetAppName = targetAppName,
            )
            setAddRuleDialogVisible(false)
        }
    }

    fun deleteChainRule(id: Long) {
        viewModelScope.launch {
            repository.deleteChainRule(id)
        }
    }
}
