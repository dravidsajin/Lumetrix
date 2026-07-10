package com.lumetrix.statsmanager.domain.model

enum class FocusState {
    Setup,
    Active,
    Completed
}

data class FocusUiState(
    val state: FocusState = FocusState.Setup,
    val selectedDurationMinutes: Int = 25,
    val selectedMode: String = "Deep Work",
    val remainingTimeMillis: Long = 25 * 60 * 1000L,
    val distractingAppsCount: Int = 0,

    // Feature 5: Focus Points Economy
    val focusPointsBalance: Int = 0,
    val pointsJustEarned: Int = 0,   // animates on session complete

    // Feature 6: App Chain Rules
    val chainRules: List<AppChainRule> = emptyList(),

    // Feature 7: Pomodoro Stats
    val recentSessions: List<FocusSessionItem> = emptyList(),
    val weeklyCompletedSessions: Int = 0,
    val weeklySuccessRate: Int = 0,
) {
    val remainingTimeLabel: String
        get() {
            val totalSeconds = remainingTimeMillis / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}

