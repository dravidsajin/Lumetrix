package com.lumetrix.statsmanager.data.tracking

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.lumetrix.statsmanager.core.time.DateUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsagePatternAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageAccessChecker: UsageAccessChecker,
) {

    private val usageStatsManager: UsageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    data class PatternAnalysis(
        val lateNightMsThisWeek: Long,
        val lateNightChangePercent: Int,
        val peakDistractionHour: Int?,
        val periodUsage: com.lumetrix.statsmanager.domain.model.PeriodUsage,
    )

    fun analyze(today: LocalDate): PatternAnalysis {
        if (!usageAccessChecker.hasUsageAccess()) {
            return PatternAnalysis(0, 0, null, com.lumetrix.statsmanager.domain.model.PeriodUsage())
        }

        val thisWeekStart = today.minusDays(6)
        val lastWeekStart = today.minusDays(13)
        val lastWeekEnd = today.minusDays(7)

        val thisWeekLateNight = collectLateNightMs(thisWeekStart, today)
        val lastWeekLateNight = collectLateNightMs(lastWeekStart, lastWeekEnd)
        val changePercent = percentChange(lastWeekLateNight, thisWeekLateNight)
        val peakHour = findPeakForegroundHour(today)
        val periodUsage = collectUsageByPeriod(thisWeekStart, today)

        return PatternAnalysis(
            lateNightMsThisWeek = thisWeekLateNight,
            lateNightChangePercent = changePercent,
            peakDistractionHour = peakHour,
            periodUsage = periodUsage,
        )
    }

    fun collectUsageByPeriod(start: LocalDate, end: LocalDate): com.lumetrix.statsmanager.domain.model.PeriodUsage {
        var morningMs = 0L
        var afternoonMs = 0L
        var eveningMs = 0L
        var nightMs = 0L

        val zone = ZoneId.systemDefault()
        var current = start
        while (!current.isAfter(end)) {
            val dayStart = DateUtils.dayStartMillis(current)
            val dayEnd = DateUtils.dayEndMillis(current)
            var sessionStart: Long? = null

            val events = usageStatsManager.queryEvents(dayStart, dayEnd)
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED,
                    UsageEvents.Event.MOVE_TO_FOREGROUND,
                    -> sessionStart = event.timeStamp

                    UsageEvents.Event.ACTIVITY_PAUSED,
                    UsageEvents.Event.MOVE_TO_BACKGROUND,
                    -> {
                        val startMs = sessionStart ?: continue
                        val endMs = event.timeStamp
                        if (endMs > startMs) {
                            var cursor = startMs
                            while (cursor < endMs) {
                                val instant = java.time.Instant.ofEpochMilli(cursor)
                                val hour = instant.atZone(zone).hour
                                val nextHour = instant.atZone(zone)
                                    .withMinute(0)
                                    .withSecond(0)
                                    .withNano(0)
                                    .plusHours(1)
                                    .toInstant()
                                    .toEpochMilli()
                                val sliceEnd = minOf(nextHour, endMs)
                                val duration = sliceEnd - cursor
                                when (hour) {
                                    in 6..11 -> morningMs += duration
                                    in 12..17 -> afternoonMs += duration
                                    in 18..21 -> eveningMs += duration
                                    else -> nightMs += duration // 22, 23, 0, 1, 2, 3, 4, 5
                                }
                                cursor = sliceEnd
                            }
                        }
                        sessionStart = null
                    }
                }
            }
            current = current.plusDays(1)
        }

        return com.lumetrix.statsmanager.domain.model.PeriodUsage(
            morningMs = morningMs,
            afternoonMs = afternoonMs,
            eveningMs = eveningMs,
            nightMs = nightMs,
        )
    }


    private fun collectLateNightMs(start: LocalDate, end: LocalDate): Long {
        var total = 0L
        var current = start
        while (!current.isAfter(end)) {
            total += collectLateNightMsForDay(current)
            current = current.plusDays(1)
        }
        return total
    }

    private fun collectLateNightMsForDay(date: LocalDate): Long {
        val zone = ZoneId.systemDefault()
        val dayStart = DateUtils.dayStartMillis(date)
        val dayEnd = DateUtils.dayEndMillis(date)
        var total = 0L
        var sessionStart: Long? = null

        val events = usageStatsManager.queryEvents(dayStart, dayEnd)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED,
                UsageEvents.Event.MOVE_TO_FOREGROUND,
                -> sessionStart = event.timeStamp

                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.MOVE_TO_BACKGROUND,
                -> {
                    val start = sessionStart ?: continue
                    total += overlapWithLateNight(start, event.timeStamp, zone)
                    sessionStart = null
                }
            }
        }
        return total
    }

    private fun overlapWithLateNight(startMs: Long, endMs: Long, zone: ZoneId): Long {
        if (endMs <= startMs) return 0L
        val date = DateUtils.millisToLocalDate(startMs)
        val lateNightRanges = listOf(
            date.atTime(22, 0).atZone(zone).toInstant().toEpochMilli() to
                date.plusDays(1).atTime(6, 0).atZone(zone).toInstant().toEpochMilli(),
            date.minusDays(1).atTime(22, 0).atZone(zone).toInstant().toEpochMilli() to
                date.atTime(6, 0).atZone(zone).toInstant().toEpochMilli(),
        )
        return lateNightRanges.sumOf { (rangeStart, rangeEnd) ->
            val overlapStart = maxOf(startMs, rangeStart)
            val overlapEnd = minOf(endMs, rangeEnd)
            (overlapEnd - overlapStart).coerceAtLeast(0L)
        }
    }

    private fun findPeakForegroundHour(date: LocalDate): Int? {
        val hourly = IntArray(24)
        val dayStart = DateUtils.dayStartMillis(date)
        val dayEnd = DateUtils.dayEndMillis(date)
        var sessionStart: Long? = null

        val events = usageStatsManager.queryEvents(dayStart, dayEnd)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED,
                UsageEvents.Event.MOVE_TO_FOREGROUND,
                -> sessionStart = event.timeStamp

                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.MOVE_TO_BACKGROUND,
                -> {
                    val start = sessionStart ?: continue
                    distributeDurationAcrossHours(start, event.timeStamp, hourly)
                    sessionStart = null
                }
            }
        }

        val peak = hourly.withIndex().maxByOrNull { it.value } ?: return null
        return if (peak.value > 0) peak.index else null
    }

    private fun distributeDurationAcrossHours(startMs: Long, endMs: Long, hourly: IntArray) {
        if (endMs <= startMs) return
        val zone = ZoneId.systemDefault()
        var cursor = startMs
        while (cursor < endMs) {
            val hour = java.time.Instant.ofEpochMilli(cursor).atZone(zone).hour
            val nextHour = java.time.Instant.ofEpochMilli(cursor)
                .atZone(zone)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .plusHours(1)
                .toInstant()
                .toEpochMilli()
            val sliceEnd = minOf(nextHour, endMs)
            hourly[hour] += ((sliceEnd - cursor) / 60_000).toInt()
            cursor = sliceEnd
        }
    }

    private fun percentChange(previous: Long, current: Long): Int {
        if (previous <= 0L) return if (current > 0L) 100 else 0
        return (((current - previous).toDouble() / previous.toDouble()) * 100).toInt()
    }
}
