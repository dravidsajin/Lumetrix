package com.lumetrix.statsmanager.data.tracking

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.lumetrix.statsmanager.core.time.DateUtils
import com.lumetrix.statsmanager.data.local.entity.AppUsageEntity
import com.lumetrix.statsmanager.data.local.entity.ScreenSessionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageStatsCollector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageAccessChecker: UsageAccessChecker,
) {

    private val usageStatsManager: UsageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private val packageManager: PackageManager = context.packageManager

    fun collectAppUsage(date: LocalDate): List<AppUsageEntity> {
        if (!usageAccessChecker.hasUsageAccess()) return emptyList()

        val dayKey = DateUtils.toDayKey(date)
        val startMs = DateUtils.dayStartMillis(date)
        val endMs = DateUtils.dayEndMillis(date)
        val now = System.currentTimeMillis()
        val syncedAt = now

        val aggregates = linkedMapOf<String, AppAggregate>()

        val usageEvents = usageStatsManager.queryEvents(startMs, endMs)
        val event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED,
                UsageEvents.Event.MOVE_TO_FOREGROUND,
                -> {
                    val packageName = event.packageName ?: continue
                    if (shouldSkipPackage(packageName)) continue
                    val aggregate = aggregates.getOrPut(packageName) {
                        AppAggregate(packageName = packageName)
                    }
                    aggregate.openSession(event.timeStamp)
                }

                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.MOVE_TO_BACKGROUND,
                -> {
                    val packageName = event.packageName ?: continue
                    aggregates[packageName]?.closeSession(event.timeStamp)
                }
            }
        }

        aggregates.values.forEach { it.closeOpenSession(endMs.coerceAtMost(now)) }

        if (aggregates.isEmpty()) {
            return collectFromUsageStatsFallback(dayKey, startMs, endMs, syncedAt, now)
        }

        return aggregates.values
            .filter { it.totalDurationMs > 0L }
            .map { aggregate ->
                AppUsageEntity(
                    packageName = aggregate.packageName,
                    appName = resolveAppName(aggregate.packageName),
                    usageDurationMs = aggregate.totalDurationMs,
                    sessionCount = aggregate.sessionCount,
                    usageDate = dayKey,
                    category = AppCategoryStorage.NEUTRAL,
                    lastSyncedAt = syncedAt,
                    createdAt = now,
                )
            }
            .sortedByDescending { it.usageDurationMs }
    }

    fun collectScreenSessions(date: LocalDate): List<ScreenSessionEntity> {
        if (!usageAccessChecker.hasUsageAccess()) return emptyList()

        val dayKey = DateUtils.toDayKey(date)
        val startMs = DateUtils.dayStartMillis(date)
        val endMs = DateUtils.dayEndMillis(date)
        val now = System.currentTimeMillis()

        val sessions = mutableListOf<ScreenSessionEntity>()
        var screenOnMs: Long? = null

        val usageEvents = usageStatsManager.queryEvents(startMs, endMs)
        val event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.SCREEN_INTERACTIVE,
                UsageEvents.Event.KEYGUARD_HIDDEN,
                -> screenOnMs = event.timeStamp

                UsageEvents.Event.SCREEN_NON_INTERACTIVE,
                UsageEvents.Event.KEYGUARD_SHOWN,
                -> {
                    val onMs = screenOnMs ?: continue
                    val offMs = event.timeStamp
                    if (offMs > onMs) {
                        sessions += ScreenSessionEntity(
                            screenOnMs = onMs,
                            screenOffMs = offMs,
                            durationMs = offMs - onMs,
                            sessionDate = dayKey,
                            createdAt = now,
                        )
                    }
                    screenOnMs = null
                }
            }
        }

        return dedupeScreenSessions(sessions)
    }

    private fun dedupeScreenSessions(
        sessions: List<ScreenSessionEntity>,
    ): List<ScreenSessionEntity> {
        if (sessions.isEmpty()) return sessions
        val sorted = sessions.sortedBy { it.screenOnMs }
        val merged = mutableListOf<ScreenSessionEntity>()
        for (session in sorted) {
            val last = merged.lastOrNull()
            if (last != null && session.screenOnMs <= last.screenOffMs) {
                val newOff = maxOf(last.screenOffMs, session.screenOffMs)
                merged[merged.lastIndex] = last.copy(
                    screenOffMs = newOff,
                    durationMs = newOff - last.screenOnMs,
                )
            } else {
                merged.add(session)
            }
        }
        return merged.filter { it.durationMs >= 5_000L }
    }

    private fun collectFromUsageStatsFallback(
        dayKey: Int,
        startMs: Long,
        endMs: Long,
        syncedAt: Long,
        now: Long,
    ): List<AppUsageEntity> {
        return usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startMs,
            endMs,
        )
            .filter { it.totalTimeInForeground > 0 && !shouldSkipPackage(it.packageName) }
            .map { stats ->
                AppUsageEntity(
                    packageName = stats.packageName,
                    appName = resolveAppName(stats.packageName),
                    usageDurationMs = stats.totalTimeInForeground,
                    sessionCount = 1,
                    usageDate = dayKey,
                    category = AppCategoryStorage.NEUTRAL,
                    lastSyncedAt = syncedAt,
                    createdAt = now,
                )
            }
            .sortedByDescending { it.usageDurationMs }
    }

    private fun resolveAppName(packageName: String): String =
        runCatching {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, 0),
            ).toString()
        }.getOrDefault(packageName)

    private fun shouldSkipPackage(packageName: String): Boolean =
        packageName == context.packageName ||
            packageName.startsWith("com.android.systemui")

    private class AppAggregate(val packageName: String) {
        var totalDurationMs: Long = 0L
            private set
        var sessionCount: Int = 0
            private set
        private var activeSinceMs: Long? = null

        fun openSession(timestampMs: Long) {
            if (activeSinceMs == null) {
                activeSinceMs = timestampMs
            }
        }

        fun closeSession(timestampMs: Long) {
            val start = activeSinceMs ?: return
            if (timestampMs > start) {
                totalDurationMs += timestampMs - start
                sessionCount++
            }
            activeSinceMs = null
        }

        fun closeOpenSession(timestampMs: Long) {
            if (activeSinceMs != null) {
                closeSession(timestampMs)
            }
        }
    }
}
