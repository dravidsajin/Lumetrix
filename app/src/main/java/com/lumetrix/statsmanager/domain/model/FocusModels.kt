package com.lumetrix.statsmanager.domain.model

/** Presentation model for a single completed focus session in history UI. */
data class FocusSessionItem(
    val id: Long,
    val mode: String,
    val plannedDurationMin: Int,
    val actualDurationMin: Int,
    val wasCompleted: Boolean,
    val pointsEarned: Int,
    /** Human-readable date, e.g. "Today", "Yesterday", "Mon Jul 7" */
    val dateLabel: String,
    /** Human-readable time, e.g. "9:30 AM" */
    val timeLabel: String,
)

/** Domain model for an App Chain Rule shown in Focus UI. */
data class AppChainRule(
    val id: Long,
    val gatePackage: String,
    val gateAppName: String,
    val gateDurationMin: Int,
    val targetPackage: String,
    val targetAppName: String,
    val isEnabled: Boolean,
    /** Progress toward gate today (0..1). */
    val gateProgress: Float = 0f,
    /** Remaining minutes needed to satisfy the gate. */
    val remainingMin: Int = 0,
    val isSatisfied: Boolean = false,
)

/** Result of evaluating a chain rule for a specific target app. */
data class ChainEvalResult(
    val isAllowed: Boolean,
    val gateAppName: String = "",
    val gateDurationMin: Int = 0,
    val remainingMin: Int = 0,
    val gateProgress: Float = 0f,
)
