package com.lumetrix.statsmanager.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumetrix.statsmanager.core.time.DateUtils
import com.lumetrix.statsmanager.data.local.dao.FocusSessionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: String, // "daily", "weekly", "longterm"
    val type: String, // "progress", "steps", "boolean"
    val current: Float,
    val target: Float,
    val unit: String,
    val accentColorHex: String,
    val weekChecks: List<Boolean> = List(7) { false },
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompleted: Int = 0
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val focusSessionDao: FocusSessionDao
) : ViewModel() {

    private val userGoals = MutableStateFlow<List<GoalItem>>(getDefaultGoals())

    // Dynamically observe focus sessions to auto-increment progress for focus goals
    val goalsState: StateFlow<List<GoalItem>> = combine(
        userGoals,
        focusSessionDao.observeSessionsForDate(DateUtils.toDayKey(DateUtils.today()))
    ) { goals, todaySessions ->
        val todayFocusMin = todaySessions.filter { it.wasCompleted }.sumOf {
            ((it.endTimeMs - it.startTimeMs) / 60000).toInt()
        }.toFloat()

        goals.map { goal ->
            if (goal.id == "focus_study" || goal.id == "focus_read") {
                val calculatedCurrent = if (goal.id == "focus_read") {
                    (todayFocusMin.coerceAtMost(goal.target))
                } else {
                    (todayFocusMin.coerceAtMost(goal.target))
                }
                goal.copy(current = calculatedCurrent)
            } else {
                goal
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun incrementGoalProgress(id: String, amount: Float) {
        viewModelScope.launch {
            userGoals.value = userGoals.value.map { goal ->
                if (goal.id == id) {
                    val nextVal = (goal.current + amount).coerceAtMost(goal.target)
                    val isJustCompleted = nextVal >= goal.target && goal.current < goal.target
                    goal.copy(
                        current = nextVal,
                        totalCompleted = if (isJustCompleted) goal.totalCompleted + 1 else goal.totalCompleted,
                        streak = if (isJustCompleted) goal.streak + 1 else goal.streak
                    )
                } else goal
            }
        }
    }

    fun toggleGoalCheck(id: String, dayIndex: Int) {
        viewModelScope.launch {
            userGoals.value = userGoals.value.map { goal ->
                if (goal.id == id) {
                    val updatedChecks = goal.weekChecks.toMutableList()
                    val prev = updatedChecks[dayIndex]
                    updatedChecks[dayIndex] = !prev
                    val totalChecked = updatedChecks.count { it }
                    goal.copy(
                        weekChecks = updatedChecks,
                        totalCompleted = totalChecked,
                        current = totalChecked.toFloat()
                    )
                } else goal
            }
        }
    }

    fun addGoal(title: String, desc: String, icon: String, target: Float, category: String) {
        viewModelScope.launch {
            val newGoal = GoalItem(
                id = "custom_" + System.currentTimeMillis(),
                title = title,
                description = desc,
                icon = icon,
                category = category,
                type = "progress",
                current = 0f,
                target = target,
                unit = "mins",
                accentColorHex = "#8B7CFF"
            )
            userGoals.value = userGoals.value + newGoal
        }
    }

    private fun getDefaultGoals(): List<GoalItem> {
        return listOf(
            GoalItem("focus_read", "Read for 30 mins", "Build the habit of reading daily", "📖", "daily", "progress", 0f, 30f, "mins", "#5ED5FF", listOf(true, true, true, true, false, false, false), 4, 7, 28),
            GoalItem("water_daily", "Drink 2L Water", "Stay hydrated throughout the day", "💧", "daily", "progress", 1.4f, 2f, "L", "#8B7CFF", listOf(true, true, false, true, true, false, false), 3, 5, 45),
            GoalItem("steps_daily", "Walk 5,000 steps", "Keep active with daily walking", "🚶", "daily", "steps", 4043f, 5000f, "steps", "#FFB74D", listOf(true, true, true, false, false, false, false), 3, 10, 60),
            GoalItem("phone_hygiene", "No phone after 10 PM", "Better sleep hygiene", "📵", "daily", "boolean", 1f, 1f, "", "#00E0A4", listOf(true, true, true, true, true, false, false), 5, 12, 35),
            GoalItem("workout_weekly", "Exercise 5x a week", "Build consistent fitness routine", "🏋️", "weekly", "progress", 3f, 5f, "sessions", "#E040FB", listOf(true, false, true, false, true, false, false), 2, 4, 15),
            GoalItem("focus_study", "Learn Kotlin 2h", "Study Kotlin weekly", "💻", "weekly", "progress", 0f, 2f, "hrs", "#8B7CFF", listOf(true, true, false, false, false, false, false), 1, 3, 8),
            GoalItem("marathon_training", "Run a Marathon", "Train for 42km run", "🏃", "longterm", "progress", 28f, 42f, "km", "#FF5252", streak = 12, bestStreak = 12, totalCompleted = 0),
            GoalItem("book_challenge", "Read 52 Books", "One book per week challenge", "📚", "longterm", "progress", 23f, 52f, "books", "#FFB74D", streak = 23, bestStreak = 23, totalCompleted = 23)
        )
    }
}
