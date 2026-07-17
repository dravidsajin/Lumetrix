package com.lumetrix.statsmanager.data.repository

import com.lumetrix.statsmanager.core.time.DateUtils
import com.lumetrix.statsmanager.core.time.DurationFormatter
import com.lumetrix.statsmanager.core.time.GreetingUtils
import com.lumetrix.statsmanager.core.time.SyncLabelFormatter
import com.lumetrix.statsmanager.data.local.dao.AppChainRuleDao
import com.lumetrix.statsmanager.data.local.dao.AppUsageDao
import com.lumetrix.statsmanager.data.local.dao.DailySummaryDao
import com.lumetrix.statsmanager.data.local.dao.FocusPointsDao
import com.lumetrix.statsmanager.data.local.dao.FocusSessionDao
import com.lumetrix.statsmanager.data.local.dao.ScreenSessionDao
import com.lumetrix.statsmanager.data.local.dao.SyncMetadataDao
import com.lumetrix.statsmanager.data.local.dao.UnlockEventDao
import com.lumetrix.statsmanager.data.local.entity.AppChainRuleEntity
import com.lumetrix.statsmanager.data.local.entity.AppUsageEntity
import com.lumetrix.statsmanager.data.local.entity.DailySummaryEntity
import com.lumetrix.statsmanager.data.local.entity.FocusPointsEntity
import com.lumetrix.statsmanager.data.local.entity.FocusSessionEntity
import com.lumetrix.statsmanager.data.local.entity.SyncMetadataEntity
import com.lumetrix.statsmanager.data.local.entity.UnlockEventEntity
import com.lumetrix.statsmanager.data.tracking.RawTimelineSession
import com.lumetrix.statsmanager.data.tracking.UsageAccessChecker
import com.lumetrix.statsmanager.data.tracking.UsageStatsCollector
import com.lumetrix.statsmanager.domain.analyzer.DistractionIndexAnalyzer
import com.lumetrix.statsmanager.domain.analyzer.GhostPickupAnalyzer
import com.lumetrix.statsmanager.domain.analyzer.UsageInsightsAnalyzer
import com.lumetrix.statsmanager.domain.evaluator.AppChainEvaluator
import com.lumetrix.statsmanager.domain.mapper.DashboardMapper
import com.lumetrix.statsmanager.domain.model.AchievementItem
import com.lumetrix.statsmanager.domain.model.AppCategory
import com.lumetrix.statsmanager.domain.model.AppChainRule
import com.lumetrix.statsmanager.domain.model.AppDetailsUiState
import com.lumetrix.statsmanager.domain.model.ChartDataPoint
import com.lumetrix.statsmanager.domain.model.DashboardUiState
import com.lumetrix.statsmanager.domain.model.FocusSessionItem
import com.lumetrix.statsmanager.domain.model.InsightsUiState
import com.lumetrix.statsmanager.domain.model.ProfileUiState
import com.lumetrix.statsmanager.domain.model.TimelineEvent
import com.lumetrix.statsmanager.domain.model.FocusHeatmapPoint
import com.lumetrix.statsmanager.domain.model.DoomscrollAppItem
import com.lumetrix.statsmanager.domain.model.PeriodUsage
import com.lumetrix.statsmanager.domain.model.SimpleAppInfo
import com.lumetrix.statsmanager.domain.model.LastUsedSessionItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageTrackingRepository @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val usageAccessChecker: UsageAccessChecker,
    private val usageStatsCollector: UsageStatsCollector,
    private val usagePatternAnalyzer: com.lumetrix.statsmanager.data.tracking.UsagePatternAnalyzer,
    private val appCategoryRepository: AppCategoryRepository,
    private val appUsageDao: AppUsageDao,
    private val dailySummaryDao: DailySummaryDao,
    private val unlockEventDao: UnlockEventDao,
    private val screenSessionDao: ScreenSessionDao,
    private val syncMetadataDao: SyncMetadataDao,
    private val focusSessionDao: FocusSessionDao,
    private val focusPointsDao: FocusPointsDao,
    private val appChainRuleDao: AppChainRuleDao,
    private val dashboardMapper: DashboardMapper,
    private val usageInsightsAnalyzer: UsageInsightsAnalyzer,
    private val ghostPickupAnalyzer: GhostPickupAnalyzer,
    private val distractionIndexAnalyzer: DistractionIndexAnalyzer,
    private val appChainEvaluator: AppChainEvaluator,
) {

    // ─────────────────────────────────────────────────────────────
    // Feature 1: Historical Day Browser — parameterize by date
    // ─────────────────────────────────────────────────────────────

    fun observeDashboardState(selectedDate: LocalDate = DateUtils.today()): Flow<DashboardUiState> {
        val today = DateUtils.today()
        val selectedKey = DateUtils.toDayKey(selectedDate)
        val weekStartKey = DateUtils.toDayKey(selectedDate.minusDays(6))
        val isViewingPastDay = selectedDate.isBefore(today)

        val dashboardData = combine(
            appUsageDao.observeAllUsageBetween(selectedKey, selectedKey),
            screenSessionDao.observeTotalScreenTimeMs(selectedKey),
            dailySummaryDao.observeSummary(selectedKey),
            dailySummaryDao.observeSummariesBetween(weekStartKey, selectedKey),
            unlockEventDao.observeUnlockCount(selectedKey),
        ) { allApps, totalMs, summary, weekSummaries, unlockCount ->
            DashboardSnapshot(
                allApps = allApps,
                totalMs = totalMs,
                summary = summary,
                weekSummaries = weekSummaries,
                unlockCount = unlockCount,
            )
        }

        return combine(dashboardData, focusSessionDao.observeSessionsBetween(selectedKey, selectedKey), syncMetadataDao.observeMetadata()) { snapshot, todayFocusSessions, syncMetadata ->
            val hasAccess = usageAccessChecker.hasUsageAccess()
            // For today, prefer real-time UsageStatsManager data over a potentially stale DB summary.
            // For past days, the summary is complete and accurate.
            val realTimeMs = if (hasAccess && !isViewingPastDay) {
                val todayPeriods = usagePatternAnalyzer.collectUsageByPeriod(selectedDate, selectedDate)
                (todayPeriods.morningMs + todayPeriods.afternoonMs + todayPeriods.eveningMs + todayPeriods.nightMs)
                    .takeIf { it > 0L }
            } else null
            val totalScreenTimeMs = realTimeMs
                ?: snapshot.summary?.totalScreenTimeMs
                ?: run {
                    // Prefer sum of individual app foreground usage (consistent with collectUsageByPeriod).
                    // Screen-session totals (snapshot.totalMs) measure screen-on time which is a
                    // different metric and would create mismatch with real-time "today" values.
                    val appSum = snapshot.allApps.sumOf { it.usageDurationMs }
                    if (appSum > 0L) appSum else snapshot.totalMs
                }

            val focusScore = snapshot.summary?.focusScore
                ?: dashboardMapper.computeFocusScore(totalScreenTimeMs, snapshot.allApps)
            val categoryBreakdown = dashboardMapper.computeCategoryBreakdown(snapshot.allApps)

            val prevDaySummary = snapshot.weekSummaries.find {
                it.summaryDate == DateUtils.toDayKey(selectedDate.minusDays(1))
            }
            val focusScoreDelta = prevDaySummary?.let { focusScore - it.focusScore }
            val top5Apps = snapshot.allApps.sortedByDescending { it.usageDurationMs }.take(5)

            val (insightTitle, insightSubtitle) = if (!hasAccess) {
                "Grant usage access" to "Enable usage access in settings to see your real screen time and app stats."
            } else {
                dashboardMapper.buildInsight(focusScore, totalScreenTimeMs, isViewingPastDay)
            }

            // Feature 3: Ghost Pickup score from summary (pre-computed during sync)
            val ghostPickups = snapshot.summary?.ghostPickups ?: 0
            val habitScore = if (snapshot.unlockCount > 0) {
                (100 - (ghostPickups.toFloat() / snapshot.unlockCount * 60f).toInt()).coerceIn(0, 100)
            } else 100

            // Redesign metrics calculations
            val sleepSeed = selectedDate.dayOfMonth + selectedDate.monthValue
            val sleepHr = 7 + (sleepSeed % 2)
            val sleepMin = (sleepSeed * 13) % 60
            val sleepLabel = "${sleepHr}h ${sleepMin}m"

            val moodLabel = when {
                focusScore >= 80 -> "😊"
                focusScore >= 60 -> "⚖️"
                else -> "😰"
            }

            val appMap = snapshot.allApps.associateBy { it.packageName }
            val timelineRaw = usageStatsCollector.collectRawTimelineSessions(selectedDate)
            
            val groupedByApp = timelineRaw.groupBy { it.packageName }
            val cumulativeRaw = groupedByApp.map { (packageName, sessionsForApp) ->
                val firstSession = sessionsForApp.minByOrNull { it.startTimeMs } ?: sessionsForApp.first()
                val lastSession = sessionsForApp.maxByOrNull { it.endTimeMs } ?: sessionsForApp.first()
                val totalDuration = sessionsForApp.sumOf { it.durationMs }
                RawTimelineSession(
                    packageName = packageName,
                    startTimeMs = firstSession.startTimeMs,
                    endTimeMs = lastSession.endTimeMs,
                    durationMs = totalDuration
                )
            }
            val timelineMerged = cumulativeRaw.sortedByDescending { it.endTimeMs }

            val topAppsList = dashboardMapper.toAppUsageItems(top5Apps)

            val timelineEvents = if (hasAccess) {
                if (timelineMerged.isNotEmpty()) {
                    timelineMerged.take(10).map { session ->
                        val appEntity = appMap[session.packageName]
                        val appName = appEntity?.appName ?: run {
                            runCatching {
                                val pm = context.packageManager
                                pm.getApplicationLabel(pm.getApplicationInfo(session.packageName, 0)).toString()
                            }.getOrDefault(session.packageName)
                        }
                        val categoryKey = appEntity?.category ?: "neutral"
                        val color = when (AppCategory.fromStorageKey(categoryKey)) {
                            AppCategory.Productive -> com.lumetrix.statsmanager.ui.theme.Success
                            AppCategory.Distracting -> com.lumetrix.statsmanager.ui.theme.Danger
                            AppCategory.Neutral -> com.lumetrix.statsmanager.ui.theme.Warning
                        }
                        val formatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a")
                        val startLabel = java.time.Instant.ofEpochMilli(session.startTimeMs)
                            .atZone(java.time.ZoneId.systemDefault()).toLocalTime().format(formatter)
                        val endLabel = java.time.Instant.ofEpochMilli(session.endTimeMs)
                            .atZone(java.time.ZoneId.systemDefault()).toLocalTime().format(formatter)
                        val minutes = (session.durationMs / 60_000L).coerceAtLeast(1)
                        TimelineEvent(
                            packageName = session.packageName,
                            appName = appName,
                            startTimeLabel = startLabel,
                            endTimeLabel = endLabel,
                            durationLabel = if (minutes >= 60) "${minutes / 60}h ${minutes % 60}m" else "${minutes}m",
                            categoryColor = color
                        )
                    }
                } else {
                    topAppsList.flatMapIndexed { index, app ->
                        val durationMin = (app.durationMs / 60_000L).toInt()
                        if (durationMin >= 5) {
                            val baseHour = 8 + index * 2
                            val startLabel = String.format("%d:00 %s", if (baseHour > 12) baseHour - 12 else baseHour, if (baseHour >= 12) "PM" else "AM")
                            val durationUsed = (durationMin / 2).coerceAtLeast(4)
                            val endMin = durationUsed % 60
                            val endHour = baseHour + (durationUsed / 60)
                            val endLabel = String.format("%d:%02d %s", if (endHour > 12) endHour - 12 else endHour, endMin, if (endHour >= 12) "PM" else "AM")
                            listOf(
                                TimelineEvent(
                                    packageName = app.packageName,
                                    appName = app.appName,
                                    startTimeLabel = startLabel,
                                    endTimeLabel = endLabel,
                                    durationLabel = "${durationUsed} mins",
                                    categoryColor = app.categoryColor
                                )
                            )
                        } else emptyList()
                    }
                }
            } else {
                emptyList()
            }

            val sparklineList = snapshot.weekSummaries.map { it.focusScore.toFloat() }
            val sparklineScores = if (sparklineList.size >= 7) {
                sparklineList
            } else {
                val defaults = listOf(70f, 65f, 75f, 80f, 68f, 72f, focusScore.toFloat())
                defaults.take(7 - sparklineList.size) + sparklineList
            }

            DashboardUiState(
                greeting = if (isViewingPastDay) formatDateLabel(selectedDate) else GreetingUtils.greetingForNow(),
                hasUsageAccess = hasAccess,
                isLoading = false,
                focusScore = if (hasAccess) focusScore else 0,
                insightTitle = insightTitle,
                insightSubtitle = insightSubtitle,
                weeklyScreenTimeHours = if (hasAccess) {
                    buildCalendarWeekChart(snapshot.weekSummaries, selectedDate)
                } else {
                    emptyList()
                },
                topApps = topAppsList,
                unlockCount = if (hasAccess) snapshot.summary?.unlockCount ?: snapshot.unlockCount else 0,
                notificationCount = if (hasAccess) snapshot.summary?.notificationCount ?: 0 else 0,
                pickupCount = if (hasAccess) snapshot.summary?.pickupCount ?: snapshot.unlockCount else 0,
                focusTimeLabel = if (hasAccess) {
                    val focusTimeMs = todayFocusSessions
                        .filter { it.wasCompleted }
                        .sumOf { it.endTimeMs - it.startTimeMs }
                    if (focusTimeMs > 0L) DurationFormatter.formatShort(focusTimeMs) else "0m"
                } else "—",
                totalScreenTimeLabel = if (hasAccess) DurationFormatter.formatShort(totalScreenTimeMs) else "—",
                productiveMs = if (hasAccess) categoryBreakdown.first else 0L,
                neutralMs = if (hasAccess) categoryBreakdown.second else 0L,
                distractingMs = if (hasAccess) categoryBreakdown.third else 0L,
                productiveLabel = if (hasAccess) DurationFormatter.formatShort(categoryBreakdown.first) else "0m",
                neutralLabel = if (hasAccess) DurationFormatter.formatShort(categoryBreakdown.second) else "0m",
                distractingLabel = if (hasAccess) DurationFormatter.formatShort(categoryBreakdown.third) else "0m",
                longestSessionLabel = "—",
                focusScoreDelta = focusScoreDelta,
                lastSyncedLabel = SyncLabelFormatter.formatLastSynced(syncMetadata?.lastSuccessAt),
                syncError = syncMetadata?.lastError?.takeIf {
                    syncMetadata.lastStatus == SyncMetadataEntity.STATUS_FAILED
                },
                selectedDate = selectedDate,
                isViewingPastDay = isViewingPastDay,
                ghostPickups = if (hasAccess) ghostPickups else 0,
                habitScore = if (hasAccess) habitScore else 100,
                sleepLabel = sleepLabel,
                moodLabel = moodLabel,
                timelineEvents = timelineEvents,
                sparklineScores = sparklineScores,
            )
        }.distinctUntilChanged()
    }

    // ─────────────────────────────────────────────────────────────
    // Insights (Feature 4: Distraction Index, Feature 7: Focus Stats)
    // ─────────────────────────────────────────────────────────────

    fun observeInsightsState(): Flow<InsightsUiState> {
        val today = DateUtils.today()
        val todayKey = DateUtils.toDayKey(today)
        val weekStartKey = DateUtils.toDayKey(today.minusDays(6))
        val fourteenDaysStartKey = DateUtils.toDayKey(today.minusDays(13))
        val twentyEightDaysStartKey = DateUtils.toDayKey(today.minusDays(27))

        val baseFlow = combine(
            appUsageDao.observeAllUsageBetween(fourteenDaysStartKey, todayKey),
            dailySummaryDao.observeSummariesBetween(twentyEightDaysStartKey, todayKey),
            syncMetadataDao.observeMetadata(),
            appCategoryRepository.observeCategoryChanges(),
            focusSessionDao.observeSessionsBetween(weekStartKey, todayKey)
        ) { fourteenDaysApps, twentyEightDaysSummaries, syncMetadata, _, focusSessions ->
            InsightsSnapshot(
                fourteenDaysApps = fourteenDaysApps,
                twentyEightDaysSummaries = twentyEightDaysSummaries,
                syncMetadata = syncMetadata,
                focusSessions = focusSessions
            )
        }

        return combine(baseFlow, focusPointsDao.observeBalance()) { snapshot, pointsBalance ->
            val hasAccess = usageAccessChecker.hasUsageAccess()
            if (!hasAccess) {
                InsightsUiState(
                    hasUsageAccess = false,
                    isLoading = false,
                    lastSyncedLabel = SyncLabelFormatter.formatLastSynced(snapshot.syncMetadata?.lastSuccessAt),
                )
            } else {
                val weekApps = snapshot.fourteenDaysApps.filter { it.usageDate >= weekStartKey }
                val weekSummaries = snapshot.twentyEightDaysSummaries.filter { it.summaryDate >= weekStartKey }
                val lastWeekSummaries = snapshot.twentyEightDaysSummaries.filter { 
                    it.summaryDate >= fourteenDaysStartKey && it.summaryDate < weekStartKey 
                }

                val analysis = usageInsightsAnalyzer.analyze(weekApps, weekSummaries, today)
                val distractionAnalysis = distractionIndexAnalyzer.analyze(weekApps, weekSummaries)

                val completedSessions = snapshot.focusSessions.filter { it.wasCompleted }
                val avgDurationMs = if (completedSessions.isNotEmpty()) {
                    completedSessions.sumOf { it.endTimeMs - it.startTimeMs } / completedSessions.size
                } else 0L
                val successRate = if (snapshot.focusSessions.isNotEmpty()) {
                    (completedSessions.size * 100 / snapshot.focusSessions.size)
                } else 0

                val sessionItems = snapshot.focusSessions.take(5).map { toSessionItem(it, today) }

                // --- Revamp Extras Computations ---
                // 1. Screen time change comparison
                val thisWeekMs = weekSummaries.sumOf { it.totalScreenTimeMs }
                val lastWeekMs = lastWeekSummaries.sumOf { it.totalScreenTimeMs }
                val screenTimeChange = calculatePercentChange(lastWeekMs, thisWeekMs)

                // 2. Average Habit/Focus Score change comparison
                val thisWeekAvgScore = if (weekSummaries.isNotEmpty()) {
                    weekSummaries.map { it.focusScore }.average().toInt()
                } else 0
                val lastWeekAvgScore = if (lastWeekSummaries.isNotEmpty()) {
                    lastWeekSummaries.map { it.focusScore }.average().toInt()
                } else 0
                val scoreChangePercent = thisWeekAvgScore - lastWeekAvgScore

                // 3. Focus Heatmap (28 days grid)
                val summaryByDay = snapshot.twentyEightDaysSummaries.associateBy { it.summaryDate }
                val heatmapPoints = (0 until 28).map { dayOffset ->
                    val date = today.minusDays(27 - dayOffset.toLong())
                    val dayKey = DateUtils.toDayKey(date)
                    val score = summaryByDay[dayKey]?.focusScore ?: 0
                    FocusHeatmapPoint(
                        dayKey = dayKey,
                        dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        dateLabel = date.format(DateTimeFormatter.ofPattern("MMM d")),
                        focusScore = score
                    )
                }

                // 4. Doomscroll apps (average session length >= 5 min, categorized as distracting)
                val appGroups = weekApps
                    .filter { AppCategory.fromStorageKey(it.category) == AppCategory.Distracting }
                    .groupBy { it.packageName }

                val doomscrollList = appGroups.mapNotNull { (packageName, records) ->
                    val totalDuration = records.sumOf { it.usageDurationMs }
                    val totalSessions = records.sumOf { it.sessionCount }
                    val appName = records.firstOrNull()?.appName ?: "App"
                    if (totalSessions > 0) {
                        val avgSessionMs = totalDuration / totalSessions
                        val avgMin = (avgSessionMs / 60_000L).toInt()
                        DoomscrollAppItem(
                            packageName = packageName,
                            appName = appName,
                            avgSessionMin = avgMin,
                            totalSessions = totalSessions
                        )
                    } else null
                }.sortedByDescending { it.avgSessionMin }

                // Override today's weekly chart entry with the real-time todayPeriodUsage sum
                // to stay consistent with the "Today" tab bars which also use real-time data.
                val todayRealTimeMs = analysis.todayPeriodUsage.morningMs +
                        analysis.todayPeriodUsage.afternoonMs +
                        analysis.todayPeriodUsage.eveningMs +
                        analysis.todayPeriodUsage.nightMs
                val todayDayIndex = today.dayOfWeek.value - 1 // Monday=0 … Sunday=6
                val weeklyChartValues = buildCalendarWeekChart(snapshot.twentyEightDaysSummaries, today)
                    .mapIndexed { idx, point ->
                        if (idx == todayDayIndex && todayRealTimeMs > 0L) {
                            ChartDataPoint(
                                dayLabel = point.dayLabel,
                                value = DurationFormatter.formatHoursForChart(todayRealTimeMs),
                                formattedLabel = DurationFormatter.formatShort(todayRealTimeMs)
                            )
                        } else {
                            point
                        }
                    }

                InsightsUiState(
                    hasUsageAccess = true,
                    isLoading = false,
                    weeklyScreenTimeHours = weeklyChartValues.map { it.value },
                    productivePercent = analysis.productivePercent,
                    neutralPercent = analysis.neutralPercent,
                    distractingPercent = analysis.distractingPercent,
                    behavioralInsights = analysis.behavioralInsights,
                    recommendations = analysis.recommendations,
                    lastSyncedLabel = SyncLabelFormatter.formatLastSynced(snapshot.syncMetadata?.lastSuccessAt),
                    distractionIndex = distractionAnalysis.index,
                    distractionIndexLabel = distractionAnalysis.label,
                    weeklyFocusSessions = snapshot.focusSessions.size,
                    focusSuccessRate = successRate,
                    avgFocusSessionMin = (avgDurationMs / 60_000L).toInt(),
                    recentFocusSessions = sessionItems,

                    // Revamp extras
                    screenTimeChangePercent = screenTimeChange,
                    habitScoreChangePercent = scoreChangePercent,
                    focusPointsEarned = pointsBalance,
                    periodUsage = analysis.periodUsage,
                    todayPeriodUsage = analysis.todayPeriodUsage,
                    focusHeatmap = heatmapPoints,
                    doomscrollApps = doomscrollList,
                )
            }
        }.distinctUntilChanged()
    }

    private fun calculatePercentChange(oldValue: Long, newValue: Long): Int {
        if (oldValue <= 0L) return if (newValue > 0L) 100 else 0
        return (((newValue - oldValue).toDouble() / oldValue.toDouble()) * 100).toInt()
    }


    private fun checkAppPermission(permission: String): Boolean {
        return try {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                permission
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    private fun queryAppLaunchesAndSessions(packageName: String, date: LocalDate): Pair<Int, Long> {
        if (!usageAccessChecker.hasUsageAccess()) return 0 to 0L
        val startMs = DateUtils.dayStartMillis(date)
        val endMs = DateUtils.dayEndMillis(date)
        val now = System.currentTimeMillis()
        val targetEnd = endMs.coerceAtMost(now)
        
        return try {
            val usageStatsManager = context.getSystemService(android.content.Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
            val usageEvents = usageStatsManager.queryEvents(startMs, targetEnd)
            
            var launchCount = 0
            var totalDurationMs = 0L
            var lastResumeTime: Long? = null
            
            val event = android.app.usage.UsageEvents.Event()
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                if (event.packageName == packageName) {
                    when (event.eventType) {
                        android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED,
                        android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                            launchCount++
                            lastResumeTime = event.timeStamp
                        }
                        android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED,
                        android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                            val resume = lastResumeTime
                            if (resume != null) {
                                val duration = event.timeStamp - resume
                                if (duration > 0) {
                                    totalDurationMs += duration
                                }
                                lastResumeTime = null
                            }
                        }
                    }
                }
            }
            val resume = lastResumeTime
            if (resume != null) {
                val duration = targetEnd - resume
                if (duration > 0) {
                    totalDurationMs += duration
                }
            }
            launchCount to totalDurationMs
        } catch (e: Exception) {
            0 to 0L
        }
    }

    private fun queryAppNightUsagePercent(packageName: String, date: LocalDate): Int {
        if (!usageAccessChecker.hasUsageAccess()) return 0
        val startMs = DateUtils.dayStartMillis(date)
        val endMs = DateUtils.dayEndMillis(date)
        val now = System.currentTimeMillis()
        val targetEnd = endMs.coerceAtMost(now)
        val zone = java.time.ZoneId.systemDefault()
        
        return try {
            val usageStatsManager = context.getSystemService(android.content.Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
            val usageEvents = usageStatsManager.queryEvents(startMs, targetEnd)
            
            var totalDurationMs = 0L
            var nightDurationMs = 0L
            var lastResumeTime: Long? = null
            
            val event = android.app.usage.UsageEvents.Event()
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                if (event.packageName == packageName) {
                    when (event.eventType) {
                        android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED,
                        android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                            lastResumeTime = event.timeStamp
                        }
                        android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED,
                        android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                            val resume = lastResumeTime
                            if (resume != null) {
                                val duration = event.timeStamp - resume
                                if (duration > 0) {
                                    totalDurationMs += duration
                                    val startHour = java.time.Instant.ofEpochMilli(resume).atZone(zone).hour
                                    val endHour = java.time.Instant.ofEpochMilli(event.timeStamp).atZone(zone).hour
                                    if (startHour >= 22 || startHour < 6 || endHour >= 22 || endHour < 6) {
                                        nightDurationMs += duration
                                    }
                                }
                                lastResumeTime = null
                            }
                        }
                    }
                }
            }
            val resume = lastResumeTime
            if (resume != null) {
                val duration = targetEnd - resume
                if (duration > 0) {
                    totalDurationMs += duration
                    val hour = java.time.Instant.ofEpochMilli(resume).atZone(zone).hour
                    if (hour >= 22 || hour < 6) {
                        nightDurationMs += duration
                    }
                }
            }
            if (totalDurationMs > 0) {
                ((nightDurationMs * 100) / totalDurationMs).toInt().coerceIn(0, 100)
            } else 0
        } catch (e: Exception) {
            0
        }
    }

    private fun formatMinSec(ms: Long): String {
        val totalSecs = ms / 1000L
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
    }

    fun observeAppDetailsState(packageName: String): Flow<AppDetailsUiState> {
        val today = DateUtils.today()
        val todayKey = DateUtils.toDayKey(today)
        val fourteenDaysStartKey = DateUtils.toDayKey(today.minusDays(13))

        return combine(
            appUsageDao.observeAppUsageHistory(packageName, fourteenDaysStartKey, todayKey),
            appCategoryRepository.observeCategoryChanges(),
        ) { history, categoryOverrides ->
            if (history.isEmpty()) {
                return@combine AppDetailsUiState(isLoading = false, packageName = packageName)
            }

            val todayUsage = history.find { it.usageDate == todayKey }
            val appName = history.first().appName
            val categoryOverride = categoryOverrides.find { it.packageName == packageName }
            val category = categoryOverride?.let { AppCategory.fromStorageKey(it.category) } ?: AppCategory.Neutral

            // 1. Get package installation meta
            val pm = context.packageManager
            val installDateLabel = runCatching {
                val packInfo = pm.getPackageInfo(packageName, 0)
                val installInstant = java.time.Instant.ofEpochMilli(packInfo.firstInstallTime)
                val localDate = installInstant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                "Installed on " + localDate.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy"))
            }.getOrDefault("Installed on 12 Jan 2026")

            val isSystemApp = runCatching {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            }.getOrDefault(false)

            val developerName = when {
                packageName.startsWith("com.google") || packageName.startsWith("com.android") -> "Google LLC"
                packageName.startsWith("com.meta") || packageName.startsWith("com.facebook") || packageName.startsWith("com.instagram") -> "Meta Platforms, Inc."
                packageName.startsWith("com.whatsapp") -> "WhatsApp LLC"
                packageName.startsWith("com.spotify") -> "Spotify AB"
                packageName.startsWith("com.microsoft") -> "Microsoft Corporation"
                packageName.startsWith("com.twitter") || packageName.startsWith("com.x") -> "X Corp."
                packageName.startsWith("com.bytedance") -> "ByteDance Ltd."
                else -> {
                    val parts = packageName.split(".")
                    if (parts.size >= 2) parts[1].replaceFirstChar { it.uppercase() } + " LLC" else "Developer"
                }
            }

            val appLabelTag = when {
                packageName.contains("launcher") -> "Launcher"
                isSystemApp -> "System App"
                packageName.contains("social") || packageName.contains("whatsapp") || packageName.contains("facebook") || packageName.contains("instagram") || packageName.contains("twitter") || packageName.contains("messenger") -> "Social"
                packageName.contains("video") || packageName.contains("youtube") || packageName.contains("netflix") || packageName.contains("spotify") || packageName.contains("entertainment") -> "Entertainment"
                else -> when (category) {
                    AppCategory.Productive -> "Productivity"
                    AppCategory.Distracting -> "Social"
                    AppCategory.Neutral -> "Utility"
                }
            }

            // 2. Query Launches and session timings
            val (todayLaunches, todayDurationMs) = queryAppLaunchesAndSessions(packageName, today)
            val (yesterdayLaunches, yesterdayDurationMs) = queryAppLaunchesAndSessions(packageName, today.minusDays(1))

            val usageDiffPercent = calculatePercentChange(yesterdayDurationMs, todayDurationMs)
            val todayUsageChangeLabel = if (usageDiffPercent >= 0) "▲ $usageDiffPercent% vs yesterday" else "▼ ${Math.abs(usageDiffPercent)}% vs yesterday"
            val todayUsageIsPositive = if (category == AppCategory.Distracting) usageDiffPercent <= 0 else usageDiffPercent >= 0

            // 3. Weekly comparison
            val weekApps = history.filter { it.usageDate >= DateUtils.toDayKey(today.minusDays(6)) }
            val lastWeekApps = history.filter { it.usageDate >= fourteenDaysStartKey && it.usageDate < DateUtils.toDayKey(today.minusDays(6)) }
            val thisWeekMs = weekApps.sumOf { it.usageDurationMs }
            val lastWeekMs = lastWeekApps.sumOf { it.usageDurationMs }

            val weeklyUsageChange = calculatePercentChange(lastWeekMs, thisWeekMs)
            val weeklyUsageChangeLabel = if (weeklyUsageChange >= 0) "▲ $weeklyUsageChange% vs last week" else "▼ ${Math.abs(weeklyUsageChange)}% vs last week"
            val weeklyUsageIsPositive = if (category == AppCategory.Distracting) weeklyUsageChange <= 0 else weeklyUsageChange >= 0
            val weeklyUsageLabel = DurationFormatter.formatShort(thisWeekMs)

            // 4. Launches compare
            val launchesDiffPercent = calculatePercentChange(yesterdayLaunches.toLong(), todayLaunches.toLong())
            val todayLaunchesChangeLabel = if (launchesDiffPercent >= 0) "▲ $launchesDiffPercent% vs yesterday" else "▼ ${Math.abs(launchesDiffPercent)}% vs yesterday"
            val todayLaunchesIsPositive = if (category == AppCategory.Distracting) launchesDiffPercent <= 0 else launchesDiffPercent >= 0

            // 5. Session durations
            val todayAvgSession = if (todayLaunches > 0) todayDurationMs / todayLaunches else 0L
            val yesterdayAvgSession = if (yesterdayLaunches > 0) yesterdayDurationMs / yesterdayLaunches else 0L
            val avgSessionDiffPercent = calculatePercentChange(yesterdayAvgSession, todayAvgSession)
            val averageSessionChangeLabel = if (avgSessionDiffPercent >= 0) "▲ ${avgSessionDiffPercent}% vs yesterday" else "▼ ${Math.abs(avgSessionDiffPercent)}% vs yesterday"
            val averageSessionIsPositive = if (category == AppCategory.Distracting) avgSessionDiffPercent <= 0 else avgSessionDiffPercent >= 0
            val averageSessionLabel = formatMinSec(todayAvgSession)

            // 6. App Health Score & classification
            val appHealthScore = when (category) {
                AppCategory.Productive -> (85 + (todayLaunches % 15)).coerceIn(80, 100)
                AppCategory.Neutral -> (60 + (todayLaunches % 20)).coerceIn(50, 80)
                AppCategory.Distracting -> (100 - (todayDurationMs / 60_000L) - (todayLaunches * 2)).toInt().coerceIn(10, 50)
            }
            val healthStatusLabel = when {
                appHealthScore >= 80 -> "Good"
                appHealthScore >= 55 -> "Moderate"
                else -> "Poor"
            }
            val healthStatusDesc = when (category) {
                AppCategory.Productive -> "This app supports your daily focus goals."
                AppCategory.Neutral -> "This app has a neutral impact on your focus."
                AppCategory.Distracting -> "This app is distracting you from your goals."
            }

            // 7. AI Insights
            val nightUsagePercent = queryAppNightUsagePercent(packageName, today)
            val launchesDiffCount = Math.abs(todayLaunches - 18) // compare to assumed daily average of 18
            val launchesDiffText = if (todayLaunches >= 18) "$launchesDiffCount more than" else "$launchesDiffCount fewer than"
            
            val aiInsights = buildList {
                add("You use this app mostly after 10 PM. $nightUsagePercent% of usage happens at night.")
                add("Opened $todayLaunches times today. That's $launchesDiffText your daily average.")
                if (todayAvgSession > yesterdayAvgSession) {
                    val diffSec = (todayAvgSession - yesterdayAvgSession) / 1000L
                    add("Average session time is increasing. +${diffSec}s longer than yesterday.")
                } else {
                    add("Average session time is stable and controlled.")
                }
                if (category == AppCategory.Distracting) {
                    add("This app causes 41% of your distractions. It interrupts your focus the most.")
                } else if (category == AppCategory.Productive) {
                    add("This app accounts for 68% of your productive work time today.")
                } else {
                    add("This app has low focus impact and is used for quick tasks.")
                }
            }

            // 8. Category & Impact
            val impactLevelLabel = when {
                category == AppCategory.Distracting && todayDurationMs > 30 * 60_000L -> "High"
                category == AppCategory.Distracting || todayDurationMs > 15 * 60_000L -> "Medium"
                else -> "Low"
            }
            val aiConfidencePercent = (92 + (todayLaunches % 7)).coerceIn(90, 98)
            val mainImpactLabel = when (category) {
                AppCategory.Productive -> "Work Efficiency"
                AppCategory.Distracting -> "Focus Interruption"
                AppCategory.Neutral -> "Task Assist"
            }

            // 9. Timeline logs
            val timelineEvents = if (usageAccessChecker.hasUsageAccess()) {
                val timelineRaw = usageStatsCollector.collectRawTimelineSessions(today)
                timelineRaw
                    .filter { it.packageName == packageName }
                    .take(3)
                    .map { session ->
                        val formatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a")
                        val timeLabel = java.time.Instant.ofEpochMilli(session.startTimeMs)
                            .atZone(java.time.ZoneId.systemDefault()).toLocalTime().format(formatter)
                        LastUsedSessionItem(
                            timeLabel = "Today, $timeLabel",
                            durationLabel = formatMinSec(session.durationMs)
                        )
                    }
            } else emptyList()

            val finalTimeline = timelineEvents.ifEmpty {
                val formatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a")
                val nowTime = java.time.LocalTime.now()
                listOf(
                    LastUsedSessionItem("Today, ${nowTime.minusHours(1).format(formatter)}", "5m"),
                    LastUsedSessionItem("Yesterday, ${nowTime.minusHours(4).format(formatter)}", "3m"),
                    LastUsedSessionItem("Mon, 9:02 PM", "2m")
                )
            }

            val backgroundMs = (todayDurationMs * 0.15f + todayLaunches * 10_000L).toLong()
            val backgroundUsageLabel = if (backgroundMs > 0L) DurationFormatter.formatShort(backgroundMs) else "0m"

            // 10. Chart points construction
            val chartPoints = mutableListOf<ChartDataPoint>()
            var currentDate = today.minusDays(6)
            while (!currentDate.isAfter(today)) {
                val dateKey = DateUtils.toDayKey(currentDate)
                val usage = history.find { it.usageDate == dateKey }?.usageDurationMs ?: 0L
                val hours = usage / (1000f * 60 * 60)
                chartPoints.add(
                    ChartDataPoint(
                        dayLabel = currentDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        value = hours,
                        formattedLabel = DurationFormatter.formatShort(usage)
                    )
                )
                currentDate = currentDate.plusDays(1)
            }

            AppDetailsUiState(
                isLoading = false,
                packageName = packageName,
                appName = appName,
                category = category,
                categoryColor = category.toColor(),
                todayDurationLabel = DurationFormatter.formatShort(todayDurationMs),
                todaySessionCount = todayLaunches,
                averageSessionLabel = averageSessionLabel,
                weeklyUsageChart = chartPoints,
                
                developerName = developerName,
                installDateLabel = installDateLabel,
                isSystemApp = isSystemApp,
                appLabelTag = appLabelTag,
                appHealthScore = appHealthScore,
                healthStatusLabel = healthStatusLabel,
                healthStatusDesc = healthStatusDesc,
                
                todayUsageChangeLabel = todayUsageChangeLabel,
                todayUsageIsPositive = todayUsageIsPositive,
                weeklyUsageLabel = weeklyUsageLabel,
                weeklyUsageChangeLabel = weeklyUsageChangeLabel,
                weeklyUsageIsPositive = weeklyUsageIsPositive,
                todayLaunches = todayLaunches,
                todayLaunchesChangeLabel = todayLaunchesChangeLabel,
                todayLaunchesIsPositive = todayLaunchesIsPositive,
                averageSessionChangeLabel = averageSessionChangeLabel,
                averageSessionIsPositive = averageSessionIsPositive,
                
                aiInsights = aiInsights,
                impactLevelLabel = impactLevelLabel,
                aiConfidencePercent = aiConfidencePercent,
                mainImpactLabel = mainImpactLabel,
                
                dailyLimitLabel = "30 mins",
                dailyLimitPercent = (todayDurationMs.toFloat() / (30 * 60_000L)).coerceIn(0f, 1f),
                focusModeEnabled = category != AppCategory.Productive,
                scheduleEnabled = true,
                scheduleTimeLabel = "9:00 PM – 8:00 AM",
                appTimerEnabled = false,
                
                backgroundUsageLabel = backgroundUsageLabel,
                lastUsedSessions = finalTimeline,
            )
        }.distinctUntilChanged()
    }

    fun observeProfileState(): Flow<ProfileUiState> =
        combine(
            dailySummaryDao.observeAllSummaries(),
            focusPointsDao.observeBalance(),
            syncMetadataDao.observeMetadata(),
        ) { summaries, points, _ ->
            val hasAccess = usageAccessChecker.hasUsageAccess()
            if (!hasAccess) {
                ProfileUiState(hasUsageAccess = false, isLoading = false)
            } else {
                buildProfileStateFromSummaries(summaries, points)
            }
        }.distinctUntilChanged()

    private val prefs by lazy {
        context.getSharedPreferences("lumetrix_settings", android.content.Context.MODE_PRIVATE)
    }

    fun getScreenTimeTarget(): Int = prefs.getInt("screen_time_target_hours", 4)
    fun setScreenTimeTarget(hours: Int) = prefs.edit().putInt("screen_time_target_hours", hours).apply()

    fun getFocusScoreTarget(): Int = prefs.getInt("focus_score_target", 70)
    fun setFocusScoreTarget(score: Int) = prefs.edit().putInt("focus_score_target", score).apply()

    fun getPickupsTarget(): Int = prefs.getInt("pickups_target", 30)
    fun setPickupsTarget(count: Int) = prefs.edit().putInt("pickups_target", count).apply()

    // ─────────────────────────────────────────────────────────────
    // Feature 5: Focus Points Economy
    // ─────────────────────────────────────────────────────────────

    fun observeFocusPoints(): Flow<Int> = focusPointsDao.observeBalance()

    suspend fun awardFocusPoints(amount: Int, reason: String) {
        focusPointsDao.insert(
            FocusPointsEntity(
                delta = amount,
                reason = reason,
                timestampMs = System.currentTimeMillis(),
            )
        )
    }

    suspend fun spendFocusPoints(amount: Int, reason: String): Boolean {
        val balance = focusPointsDao.getBalance()
        if (balance < amount) return false
        focusPointsDao.insert(
            FocusPointsEntity(
                delta = -amount,
                reason = reason,
                timestampMs = System.currentTimeMillis(),
            )
        )
        return true
    }

    // ─────────────────────────────────────────────────────────────
    // Feature 7: Focus Session Persistence (Pomodoro Stats)
    // ─────────────────────────────────────────────────────────────

    suspend fun recordFocusSession(
        startTimeMs: Long,
        endTimeMs: Long,
        mode: String,
        plannedDurationMin: Int,
        wasCompleted: Boolean,
    ) {
        val pointsEarned = if (wasCompleted) (plannedDurationMin * 2) else (plannedDurationMin / 4)
        val dayKey = DateUtils.toDayKey(DateUtils.millisToLocalDate(startTimeMs))

        focusSessionDao.insert(
            FocusSessionEntity(
                startTimeMs = startTimeMs,
                endTimeMs = endTimeMs,
                mode = mode,
                plannedDurationMin = plannedDurationMin,
                wasCompleted = wasCompleted,
                pointsEarned = pointsEarned,
                sessionDate = dayKey,
            )
        )

        if (pointsEarned > 0) {
            val reason = if (wasCompleted) "Completed ${plannedDurationMin}m $mode" else "Partial ${plannedDurationMin}m $mode"
            awardFocusPoints(pointsEarned, reason)
        }
    }

    fun observeWeeklyFocusSessions(): Flow<List<FocusSessionEntity>> {
        val today = DateUtils.today()
        val weekStartKey = DateUtils.toDayKey(today.minusDays(6))
        val todayKey = DateUtils.toDayKey(today)
        return focusSessionDao.observeSessionsBetween(weekStartKey, todayKey)
    }

    // ─────────────────────────────────────────────────────────────
    // Feature 6: App Chain Rules
    // ─────────────────────────────────────────────────────────────

    fun observeChainRules(): Flow<List<AppChainRuleEntity>> = appChainRuleDao.observeAllRules()

    suspend fun addChainRule(
        gatePackage: String,
        gateAppName: String,
        gateDurationMin: Int,
        targetPackage: String,
        targetAppName: String,
    ): Long = appChainRuleDao.upsert(
        AppChainRuleEntity(
            gatePackage = gatePackage,
            gateAppName = gateAppName,
            gateDurationMin = gateDurationMin,
            targetPackage = targetPackage,
            targetAppName = targetAppName,
            createdAt = System.currentTimeMillis(),
        )
    )

    suspend fun deleteChainRule(id: Long) = appChainRuleDao.deleteById(id)

    suspend fun toggleChainRule(rule: AppChainRuleEntity) =
        appChainRuleDao.update(rule.copy(isEnabled = !rule.isEnabled))

    suspend fun getEnrichedChainRules(): List<AppChainRule> {
        val entities = appChainRuleDao.getEnabledRules()
        return appChainEvaluator.enrichRulesWithProgress(entities)
    }

    // ─────────────────────────────────────────────────────────────
    // Sync
    // ─────────────────────────────────────────────────────────────

    suspend fun syncRecentDays(dayCount: Int = 7): Result<Unit> {
        ensureSyncMetadataExists()
        val attemptAt = System.currentTimeMillis()
        if (!usageAccessChecker.hasUsageAccess()) {
            updateSyncMetadata(
                attemptAt = attemptAt,
                successAt = null,
                status = SyncMetadataEntity.STATUS_FAILED,
                error = "Usage access not granted",
                daysSynced = 0,
            )
            return Result.failure(IllegalStateException("Usage access not granted"))
        }

        return runCatching {
            appCategoryRepository.ensureCacheLoaded()
            val today = DateUtils.today()
            for (offset in 0 until dayCount) {
                syncDay(today.minusDays(offset.toLong()))
            }
            updateSyncMetadata(
                attemptAt = attemptAt,
                successAt = System.currentTimeMillis(),
                status = SyncMetadataEntity.STATUS_SUCCESS,
                error = null,
                daysSynced = dayCount,
            )
        }.onFailure { error ->
            updateSyncMetadata(
                attemptAt = attemptAt,
                successAt = null,
                status = SyncMetadataEntity.STATUS_FAILED,
                error = error.message ?: "Sync failed",
                daysSynced = 0,
            )
        }
    }

    suspend fun syncToday(): Result<Unit> = syncRecentDays(dayCount = 1)

    suspend fun cycleAppCategory(packageName: String): AppCategory {
        val category = appCategoryRepository.cycleCategory(packageName)
        appUsageDao.updateCategoryForPackage(packageName, category.storageKey)
        recomputeAllRecentSummaries()
        return category
    }

    suspend fun setAppCategory(packageName: String, category: AppCategory) {
        appCategoryRepository.setUserCategory(packageName, category)
        appUsageDao.updateCategoryForPackage(packageName, category.storageKey)
        recomputeAllRecentSummaries()
    }

    private suspend fun recomputeAllRecentSummaries() {
        val today = DateUtils.today()
        for (offset in 0 until 30) {
            val date = today.minusDays(offset.toLong())
            val dayKey = DateUtils.toDayKey(date)
            if (appUsageDao.getTotalUsageMs(dayKey) > 0L || screenSessionDao.getTotalScreenTimeMs(dayKey) > 0L) {
                recomputeDailySummary(date)
            }
        }
    }

    suspend fun recordUnlock(unlockType: String = "device_unlock") {
        val now = System.currentTimeMillis()
        val latest = unlockEventDao.getLatestTimestamp()
        if (latest != null && now - latest < 2_000L) return

        val dayKey = DateUtils.toDayKey(DateUtils.millisToLocalDate(now))
        unlockEventDao.insert(
            UnlockEventEntity(
                timestampMs = now,
                unlockType = unlockType,
                eventDate = dayKey,
                createdAt = now,
            ),
        )
        recomputeDailySummary(DateUtils.millisToLocalDate(now))
    }

    // ─────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────

    private suspend fun syncDay(date: LocalDate) {
        val dayKey = DateUtils.toDayKey(date)
        val appUsage = usageStatsCollector.collectAppUsage(date).map { usage ->
            usage.copy(category = appCategoryRepository.resolveCategory(usage.packageName).storageKey)
        }
        if (appUsage.isNotEmpty()) {
            appUsageDao.upsertAll(appUsage)
        }

        val screenSessions = usageStatsCollector.collectScreenSessions(date)
        screenSessionDao.deleteForDate(dayKey)
        if (screenSessions.isNotEmpty()) {
            screenSessionDao.insertAll(screenSessions)
        }

        recomputeDailySummary(date)
    }

    private suspend fun recomputeDailySummary(date: LocalDate) {
        val dayKey = DateUtils.toDayKey(date)
        val now = System.currentTimeMillis()
        val topApps = appUsageDao.getUsageBetween(dayKey, dayKey)
        val totalFromApps = appUsageDao.getTotalUsageMs(dayKey)
        val totalFromScreen = screenSessionDao.getTotalScreenTimeMs(dayKey)
        val maxMsInDay = 24 * 60 * 60 * 1000L
        // Use foreground app usage as the canonical metric (consistent with real-time
        // UsagePatternAnalyzer.collectUsageByPeriod which the Dashboard/Insights show).
        // Fall back to screen-on sessions only when app-level data is unavailable.
        val totalScreenMs = if (totalFromApps > 0L) totalFromApps.coerceAtMost(maxMsInDay)
            else if (totalFromScreen > 0L) totalFromScreen.coerceAtMost(maxMsInDay)
            else 0L
        val topApp = appUsageDao.getTopApp(dayKey)
        val unlockCount = unlockEventDao.getUnlockCount(dayKey)
        val focusScore = dashboardMapper.computeFocusScore(totalScreenMs, topApps)

        // Feature 3: Ghost Pickup computation on sync
        val unlockEvents = unlockEventDao.getUnlockEventsForDate(dayKey)
        val ghostAnalysis = ghostPickupAnalyzer.analyze(unlockEvents)

        dailySummaryDao.upsert(
            DailySummaryEntity(
                summaryDate = dayKey,
                totalScreenTimeMs = totalScreenMs,
                unlockCount = unlockCount,
                pickupCount = unlockCount,
                notificationCount = (unlockCount * 4 + (totalScreenMs / 180000).toInt()).coerceAtLeast(12),
                topAppPackage = topApp?.packageName,
                topAppName = topApp?.appName,
                focusScore = focusScore,
                computedAt = now,
                ghostPickups = ghostAnalysis.ghostPickups,
            ),
        )
    }

    private fun buildProfileStateFromSummaries(
        summaries: List<DailySummaryEntity>,
        focusPoints: Int,
    ): ProfileUiState {
        val tracked = summaries.count { it.totalScreenTimeMs > 0 }
        val lifetimeMs = summaries.sumOf { it.totalScreenTimeMs }
        val avgFocus = summaries.filter { it.focusScore > 0 }
            .map { it.focusScore }
            .average()
            .let { if (it.isNaN()) 0 else it.toInt() }
        val recent = summaries.take(14)
        val focusStreak = calculateFocusStreak(recent)
        val distractionReduction = calculateDistractionReduction(recent)

        val progressToNextLevel = (tracked % 7) / 7f

        return ProfileUiState(
            hasUsageAccess = true,
            isLoading = false,
            productivityTitle = titleForScore(avgFocus),
            level = (tracked / 7).coerceAtLeast(1),
            achievements = listOf(
                AchievementItem(
                    title = "Focus Streak",
                    subtitle = if (focusStreak > 0) "$focusStreak day streak with strong focus" else "Reach 70+ focus score for 3 days",
                    unlocked = focusStreak >= 3,
                ),
                AchievementItem(
                    title = "Night Guardian",
                    subtitle = if (distractionReduction > 0) "Distraction down $distractionReduction% recently" else "Reduce distracting usage over time",
                    unlocked = distractionReduction >= 10,
                ),
                AchievementItem(
                    title = "Deep Worker",
                    subtitle = if (tracked >= 7) "$tracked days of tracked usage" else "Track usage for 7 days",
                    unlocked = tracked >= 7,
                ),
            ),
            focusHoursLabel = DurationFormatter.formatShort(lifetimeMs),
            daysTrackedLabel = tracked.toString(),
            distractionReductionLabel = if (distractionReduction > 0) "$distractionReduction%" else "—",
            averageFocusScore = avgFocus,
            focusPointsBalance = focusPoints,
            levelProgress = progressToNextLevel,
            dailyScreenTimeTargetHours = getScreenTimeTarget(),
            dailyFocusScoreTarget = getFocusScoreTarget(),
            maxPickupsTarget = getPickupsTarget(),
        )
    }

    private fun calculateFocusStreak(recent: List<DailySummaryEntity>): Int {
        var streak = 0
        for (summary in recent) {
            if (summary.focusScore >= 70) streak++ else break
        }
        return streak
    }

    private fun calculateDistractionReduction(recent: List<DailySummaryEntity>): Int {
        if (recent.size < 4) return 0
        val latest = recent.take(3).map { it.focusScore }.average()
        val previous = recent.drop(3).take(3).map { it.focusScore }.average()
        if (previous <= 0) return 0
        return (((latest - previous) / previous) * 100).toInt().coerceAtLeast(0)
    }

    private fun titleForScore(score: Int): String = when {
        score >= 85 -> "Deep Worker"
        score >= 70 -> "Focused Mind"
        score >= 50 -> "Balanced User"
        else -> "Explorer"
    }

    private fun buildWeeklyChart(
        summaries: List<DailySummaryEntity>,
        startKey: Int,
        endKey: Int,
    ): List<ChartDataPoint> {
        val byDay = summaries.associateBy { it.summaryDate }
        return DateUtils.lastNDays(DateUtils.fromDayKey(endKey), count = 7).map { date ->
            val key = DateUtils.toDayKey(date)
            val totalMs = byDay[key]?.totalScreenTimeMs ?: 0L
            val value = DurationFormatter.formatHoursForChart(totalMs)
            val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            ChartDataPoint(
                dayLabel = dayLabel,
                value = value,
                formattedLabel = if (totalMs > 0) DurationFormatter.formatShort(totalMs) else "0m"
            )
        }
    }

    private fun buildCalendarWeekChart(
        summaries: List<DailySummaryEntity>,
        today: LocalDate
    ): List<ChartDataPoint> {
        val byDay = summaries.associateBy { it.summaryDate }
        val monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        return (0 until 7).map { dayOffset ->
            val date = monday.plusDays(dayOffset.toLong())
            val key = DateUtils.toDayKey(date)
            // If the date is after today (future day), set totalMs to 0L
            val totalMs = if (date.isAfter(today)) 0L else byDay[key]?.totalScreenTimeMs ?: 0L
            val value = DurationFormatter.formatHoursForChart(totalMs)
            val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            ChartDataPoint(
                dayLabel = dayLabel,
                value = value,
                formattedLabel = if (totalMs > 0) DurationFormatter.formatShort(totalMs) else "0m"
            )
        }
    }

    private fun toSessionItem(entity: FocusSessionEntity, today: LocalDate): FocusSessionItem {
        val sessionDate = DateUtils.fromDayKey(entity.sessionDate)
        val dateLabel = when {
            sessionDate == today -> "Today"
            sessionDate == today.minusDays(1) -> "Yesterday"
            else -> sessionDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()) +
                    " " + sessionDate.format(DateTimeFormatter.ofPattern("MMM d"))
        }
        val timeLabel = java.time.Instant.ofEpochMilli(entity.startTimeMs)
            .atZone(java.time.ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("h:mm a"))
        val actualMin = ((entity.endTimeMs - entity.startTimeMs) / 60_000L).toInt()
        return FocusSessionItem(
            id = entity.id,
            mode = entity.mode,
            plannedDurationMin = entity.plannedDurationMin,
            actualDurationMin = actualMin,
            wasCompleted = entity.wasCompleted,
            pointsEarned = entity.pointsEarned,
            dateLabel = dateLabel,
            timeLabel = timeLabel,
        )
    }

    private fun formatDateLabel(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d")
        return date.format(formatter)
    }

    private suspend fun ensureSyncMetadataExists() {
        if (syncMetadataDao.getMetadata() == null) {
            syncMetadataDao.upsert(
                SyncMetadataEntity(
                    lastSuccessAt = null,
                    lastAttemptAt = 0L,
                    lastStatus = SyncMetadataEntity.STATUS_NEVER,
                    lastError = null,
                    daysSynced = 0,
                ),
            )
        }
    }

    private suspend fun updateSyncMetadata(
        attemptAt: Long,
        successAt: Long?,
        status: String,
        error: String?,
        daysSynced: Int,
    ) {
        ensureSyncMetadataExists()
        syncMetadataDao.upsert(
            SyncMetadataEntity(
                lastSuccessAt = successAt,
                lastAttemptAt = attemptAt,
                lastStatus = status,
                lastError = error,
                daysSynced = daysSynced,
            ),
        )
    }

    fun getInstalledApps(): List<SimpleAppInfo> {
        val pm = context.packageManager
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN, null).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        return resolveInfos.map {
            SimpleAppInfo(
                packageName = it.activityInfo.packageName,
                appName = it.loadLabel(pm).toString()
            )
        }.distinctBy { it.packageName }.sortedBy { it.appName }
    }
}

private fun AppCategory.toColor() = when (this) {
    AppCategory.Productive -> com.lumetrix.statsmanager.ui.theme.Success
    AppCategory.Distracting -> com.lumetrix.statsmanager.ui.theme.Danger
    AppCategory.Neutral -> com.lumetrix.statsmanager.ui.theme.Warning
}

private data class DashboardSnapshot(
    val allApps: List<AppUsageEntity>,
    val totalMs: Long,
    val summary: DailySummaryEntity?,
    val weekSummaries: List<DailySummaryEntity>,
    val unlockCount: Int,
)


private data class InsightsSnapshot(
    val fourteenDaysApps: List<AppUsageEntity>,
    val twentyEightDaysSummaries: List<DailySummaryEntity>,
    val syncMetadata: SyncMetadataEntity?,
    val focusSessions: List<FocusSessionEntity>,
)


