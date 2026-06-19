package com.lumetrix.statsmanager.domain.mapper

import com.lumetrix.statsmanager.core.time.DurationFormatter
import com.lumetrix.statsmanager.data.local.entity.AppUsageEntity
import com.lumetrix.statsmanager.domain.model.AppCategory
import com.lumetrix.statsmanager.domain.model.AppUsageItem
import com.lumetrix.statsmanager.ui.theme.Danger
import com.lumetrix.statsmanager.ui.theme.Success
import com.lumetrix.statsmanager.ui.theme.Warning
import javax.inject.Inject

class DashboardMapper @Inject constructor() {

    fun toAppUsageItems(entities: List<AppUsageEntity>): List<AppUsageItem> =
        entities.map { entity ->
            val category = AppCategory.fromStorageKey(entity.category)
            AppUsageItem(
                packageName = entity.packageName,
                appName = entity.appName,
                durationMs = entity.usageDurationMs,
                durationLabel = DurationFormatter.formatShort(entity.usageDurationMs),
                category = category,
                categoryColor = category.toColor(),
            )
        }

    fun computeFocusScore(totalMs: Long, apps: List<AppUsageEntity>): Int {
        if (totalMs <= 0L) return 100
        val distractingMs = apps
            .filter { AppCategory.fromStorageKey(it.category) == AppCategory.Distracting }
            .sumOf { it.usageDurationMs }
        val ratio = distractingMs.toFloat() / totalMs.toFloat()
        return (100 - (ratio * 100f)).toInt().coerceIn(0, 100)
    }

    fun buildInsight(focusScore: Int, totalMs: Long): Pair<String, String> {
        if (totalMs <= 0L) {
            return "No usage recorded yet" to "Use your device normally and sync again in a few minutes."
        }
        return when {
            focusScore >= 80 -> "Strong focus day" to "Your distracting app usage is low today. Keep it up."
            focusScore >= 60 -> "Balanced usage" to "You're doing well — a short focus session could boost productivity."
            else -> "High distraction detected" to "Consider a focus session to reduce time on distracting apps."
        }
    }

    fun computeCategoryBreakdown(apps: List<AppUsageEntity>): Triple<Int, Int, Int> {
        val total = apps.sumOf { it.usageDurationMs }.coerceAtLeast(1L)
        val productive = apps.filter { AppCategory.fromStorageKey(it.category) == AppCategory.Productive }
            .sumOf { it.usageDurationMs }
        val distracting = apps.filter { AppCategory.fromStorageKey(it.category) == AppCategory.Distracting }
            .sumOf { it.usageDurationMs }
        val neutral = total - productive - distracting
        return Triple(
            (productive * 100 / total).toInt(),
            (neutral * 100 / total).toInt(),
            (distracting * 100 / total).toInt(),
        )
    }

    private fun AppCategory.toColor() = when (this) {
        AppCategory.Productive -> Success
        AppCategory.Distracting -> Danger
        AppCategory.Neutral -> Warning
    }
}
