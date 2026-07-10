package com.lumetrix.statsmanager.domain.analyzer

import com.lumetrix.statsmanager.data.local.entity.AppUsageEntity
import com.lumetrix.statsmanager.data.local.entity.DailySummaryEntity
import com.lumetrix.statsmanager.domain.model.AppCategory
import javax.inject.Inject

/**
 * Computes the "Distraction Index" — a 0–100 score measuring how much of a user's
 * phone interaction is reactive (driven by distracting apps) vs. intentional.
 *
 * Higher = more distracted. 0 = fully intentional. 100 = completely reactive.
 *
 * Scoring factors:
 * 1. Ratio of distracting-app time to total screen time (50% weight)
 * 2. Unlock frequency relative to screen time (30% weight)
 * 3. Late-night usage ratio (20% weight)
 */
class DistractionIndexAnalyzer @Inject constructor() {

    data class DistractionAnalysis(
        /** 0–100; higher = more distracted */
        val index: Int,
        val label: String,
        val topDistractingAppName: String?,
        val distractingTimePercent: Int,
    )

    fun analyze(
        weekApps: List<AppUsageEntity>,
        weekSummaries: List<DailySummaryEntity>,
    ): DistractionAnalysis {
        if (weekApps.isEmpty() || weekSummaries.isEmpty()) {
            return DistractionAnalysis(0, "No Data", null, 0)
        }

        val totalMs = weekApps.sumOf { it.usageDurationMs }.coerceAtLeast(1L)
        val distractingMs = weekApps
            .filter { AppCategory.fromStorageKey(it.category) == AppCategory.Distracting }
            .sumOf { it.usageDurationMs }

        val topDistractingApp = weekApps
            .filter { AppCategory.fromStorageKey(it.category) == AppCategory.Distracting }
            .maxByOrNull { it.usageDurationMs }

        // Factor 1: Distracting time ratio (0–100)
        val distractingRatio = (distractingMs.toFloat() / totalMs * 100).toInt().coerceIn(0, 100)

        // Factor 2: Unlock frequency score
        val avgUnlocks = weekSummaries.map { it.unlockCount }.average()
        val unlockScore = when {
            avgUnlocks >= 100 -> 100
            avgUnlocks >= 60 -> 70
            avgUnlocks >= 30 -> 40
            else -> 10
        }

        // Factor 3: Late-night usage approximation
        // We don't have hourly data yet, so we use unlock count as proxy for now
        val lateNightScore = if (avgUnlocks > 50) 40 else 10

        // Weighted composite
        val index = ((distractingRatio * 0.5f) + (unlockScore * 0.3f) + (lateNightScore * 0.2f))
            .toInt().coerceIn(0, 100)

        val label = when {
            index >= 70 -> "High Distraction"
            index >= 40 -> "Moderate"
            index >= 20 -> "Focused"
            else -> "Highly Focused"
        }

        return DistractionAnalysis(
            index = index,
            label = label,
            topDistractingAppName = topDistractingApp?.appName,
            distractingTimePercent = (distractingMs.toFloat() / totalMs * 100).toInt().coerceIn(0, 100),
        )
    }
}
