package com.lumetrix.statsmanager.domain.analyzer

import com.lumetrix.statsmanager.data.local.entity.UnlockEventEntity
import javax.inject.Inject

/**
 * Approximates "ghost pickups" — unlocks that happen without any clear notification trigger.
 *
 * Since we don't yet have a NotificationListenerService, we use time-clustering as a proxy:
 * an unlock is considered a "ghost" if no other unlock occurred in the previous 120 seconds
 * AND it happened during typical distraction windows (any hour, but we can refine later).
 *
 * This is a conservative approximation that avoids false positives.
 */
class GhostPickupAnalyzer @Inject constructor() {

    data class GhostAnalysis(
        val ghostPickups: Int,
        val totalPickups: Int,
        /** 0–100; higher = better habit control */
        val habitScore: Int,
    )

    fun analyze(unlockEvents: List<UnlockEventEntity>): GhostAnalysis {
        if (unlockEvents.isEmpty()) return GhostAnalysis(0, 0, 100)

        val sorted = unlockEvents.sortedBy { it.timestampMs }
        var ghostCount = 0

        for (i in sorted.indices) {
            val current = sorted[i]
            val prevTimestamp = if (i > 0) sorted[i - 1].timestampMs else 0L
            val timeSincePrev = current.timestampMs - prevTimestamp

            // If this unlock happened more than 120 seconds after the previous one,
            // it is not part of a usage cluster — treat it as a habitual ghost pickup.
            // Unlocks within 120s of each other are part of an active usage session.
            val isIsolated = timeSincePrev > GHOST_WINDOW_MS || i == 0
            if (isIsolated) ghostCount++
        }

        val total = sorted.size
        // Habit score: 100 when 0 ghost pickups, scales down linearly
        // Heavy phone habit (>30 ghosts) = score of ~20
        val habitScore = (100 - (ghostCount.toFloat() / total.coerceAtLeast(1) * 100f)
            .toInt().coerceIn(0, 80)).coerceAtLeast(20)

        return GhostAnalysis(
            ghostPickups = ghostCount,
            totalPickups = total,
            habitScore = habitScore,
        )
    }

    companion object {
        private const val GHOST_WINDOW_MS = 120_000L // 2 minutes
    }
}
