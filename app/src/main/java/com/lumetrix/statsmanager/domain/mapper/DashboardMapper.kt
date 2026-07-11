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

    fun buildInsight(focusScore: Int, totalMs: Long, isViewingPastDay: Boolean = false): Pair<String, String> {
        if (totalMs <= 0L) {
            return "No usage recorded yet" to "Use your device normally and sync again in a few minutes."
        }
        val dayTerm = if (isViewingPastDay) "on this day" else "today"
        return when {
            focusScore >= 80 -> "Strong focus day" to "Your distracting app usage was low $dayTerm. Keep it up."
            focusScore >= 60 -> "Balanced usage" to "You did well — you maintained a healthy focus state $dayTerm."
            else -> "High distraction detected" to "You spent a lot of time on distracting apps $dayTerm."
        }
    }

    fun computeCategoryBreakdown(apps: List<AppUsageEntity>): Triple<Long, Long, Long> {
        val productive = apps.filter { AppCategory.fromStorageKey(it.category) == AppCategory.Productive }
            .sumOf { it.usageDurationMs }
        val distracting = apps.filter { AppCategory.fromStorageKey(it.category) == AppCategory.Distracting }
            .sumOf { it.usageDurationMs }
        val total = apps.sumOf { it.usageDurationMs }
        val neutral = total - productive - distracting
        return Triple(productive, neutral, distracting)
    }

    private fun AppCategory.toColor() = when (this) {
        AppCategory.Productive -> Success
        AppCategory.Distracting -> Danger
        AppCategory.Neutral -> Warning
    }
}
