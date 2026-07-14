package com.lumetrix.statsmanager.core.time

import kotlin.math.roundToInt

object DurationFormatter {

    fun formatShort(durationMs: Long): String {
        if (durationMs <= 0L) return "0m"
        val totalMinutes = (durationMs / 60_000.0).roundToInt().coerceAtLeast(1)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    fun formatHoursForChart(durationMs: Long): Float =
        (durationMs / 3_600_000f).coerceAtLeast(0f)
}
