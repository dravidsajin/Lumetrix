package com.lumetrix.statsmanager.data.repository

import com.lumetrix.statsmanager.core.time.DateUtils
import com.lumetrix.statsmanager.core.time.DurationFormatter
import com.lumetrix.statsmanager.core.time.GreetingUtils
import com.lumetrix.statsmanager.core.time.SyncLabelFormatter
import com.lumetrix.statsmanager.data.local.dao.AppUsageDao
import com.lumetrix.statsmanager.data.local.dao.DailySummaryDao
import com.lumetrix.statsmanager.data.local.dao.ScreenSessionDao
import com.lumetrix.statsmanager.data.local.dao.SyncMetadataDao
import com.lumetrix.statsmanager.data.local.dao.UnlockEventDao
import com.lumetrix.statsmanager.data.local.entity.AppUsageEntity
import com.lumetrix.statsmanager.data.local.entity.DailySummaryEntity
import com.lumetrix.statsmanager.data.local.entity.SyncMetadataEntity
import com.lumetrix.statsmanager.data.local.entity.UnlockEventEntity
import com.lumetrix.statsmanager.data.tracking.UsageAccessChecker
import com.lumetrix.statsmanager.data.tracking.UsageStatsCollector
import com.lumetrix.statsmanager.domain.analyzer.UsageInsightsAnalyzer
import com.lumetrix.statsmanager.domain.mapper.DashboardMapper
import com.lumetrix.statsmanager.domain.model.AchievementItem
import com.lumetrix.statsmanager.domain.model.DashboardUiState
import com.lumetrix.statsmanager.domain.model.InsightsUiState
import com.lumetrix.statsmanager.domain.model.ProfileUiState
import com.lumetrix.statsmanager.domain.model.AppCategory
import com.lumetrix.statsmanager.domain.model.AppDetailsUiState
import com.lumetrix.statsmanager.domain.model.ChartDataPoint
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageTrackingRepository @Inject constructor(
    private val usageAccessChecker: UsageAccessChecker,
    private val usageStatsCollector: UsageStatsCollector,
    private val appCategoryRepository: AppCategoryRepository,
    private val appUsageDao: AppUsageDao,
    private val dailySummaryDao: DailySummaryDao,
    private val unlockEventDao: UnlockEventDao,
    private val screenSessionDao: ScreenSessionDao,
    private val syncMetadataDao: SyncMetadataDao,
    private val dashboardMapper: DashboardMapper,
    private val usageInsightsAnalyzer: UsageInsightsAnalyzer,
) {

    fun observeDashboardState(): Flow<DashboardUiState> {
        val today = DateUtils.today()
        val todayKey = DateUtils.toDayKey(today)
        val weekStartKey = DateUtils.toDayKey(today.minusDays(6))

        val dashboardData = combine(
            appUsageDao.observeAllUsageBetween(todayKey, todayKey),
            screenSessionDao.observeTotalScreenTimeMs(todayKey),
            dailySummaryDao.observeSummary(todayKey),
            dailySummaryDao.observeSummariesBetween(weekStartKey, todayKey),
            unlockEventDao.observeUnlockCount(todayKey),
        ) { allApps, totalMs, summary, weekSummaries, unlockCount ->
            DashboardSnapshot(
                allApps = allApps,
                totalMs = totalMs,
                summary = summary,
                weekSummaries = weekSummaries,
                unlockCount = unlockCount,
            )
        }

        return combine(dashboardData, syncMetadataDao.observeMetadata()) { snapshot, syncMetadata ->
            val hasAccess = usageAccessChecker.hasUsageAccess()
            val focusScore = snapshot.summary?.focusScore
                ?: dashboardMapper.computeFocusScore(snapshot.totalMs, snapshot.allApps)
            val categoryBreakdown = dashboardMapper.computeCategoryBreakdown(snapshot.allApps)
            
            val yesterdaySummary = snapshot.weekSummaries.find { it.summaryDate == DateUtils.toDayKey(today.minusDays(1)) }
            val focusScoreDelta = yesterdaySummary?.let { focusScore - it.focusScore }
            val top5Apps = snapshot.allApps.sortedByDescending { it.usageDurationMs }.take(5)
            
            val (insightTitle, insightSubtitle) = if (!hasAccess) {
                "Grant usage access" to "Enable usage access in settings to see your real screen time and app stats."
            } else {
                dashboardMapper.buildInsight(focusScore, snapshot.totalMs)
            }

            DashboardUiState(
                greeting = GreetingUtils.greetingForNow(),
                hasUsageAccess = hasAccess,
                isLoading = false,
                focusScore = if (hasAccess) focusScore else 0,
                insightTitle = insightTitle,
                insightSubtitle = insightSubtitle,
                weeklyScreenTimeHours = if (hasAccess) {
                    buildWeeklyChart(snapshot.weekSummaries, weekStartKey, todayKey)
                } else {
                    emptyList()
                },
                topApps = if (hasAccess) dashboardMapper.toAppUsageItems(top5Apps) else emptyList(),
                unlockCount = if (hasAccess) snapshot.summary?.unlockCount ?: snapshot.unlockCount else 0,
                notificationCount = if (hasAccess) snapshot.summary?.notificationCount ?: 0 else 0,
                pickupCount = if (hasAccess) snapshot.summary?.pickupCount ?: snapshot.unlockCount else 0,
                focusTimeLabel = if (hasAccess) DurationFormatter.formatShort(snapshot.totalMs) else "—",
                totalScreenTimeLabel = if (hasAccess) DurationFormatter.formatShort(snapshot.totalMs) else "—",
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
            )
        }.distinctUntilChanged()
    }

    fun observeInsightsState(): Flow<InsightsUiState> {
        val today = DateUtils.today()
        val todayKey = DateUtils.toDayKey(today)
        val weekStartKey = DateUtils.toDayKey(today.minusDays(6))

        return combine(
            appUsageDao.observeAllUsageBetween(weekStartKey, todayKey),
            dailySummaryDao.observeSummariesBetween(weekStartKey, todayKey),
            syncMetadataDao.observeMetadata(),
            appCategoryRepository.observeCategoryChanges(),
        ) { weekApps, weekSummaries, syncMetadata, _ ->
            val hasAccess = usageAccessChecker.hasUsageAccess()
            if (!hasAccess) {
                InsightsUiState(
                    hasUsageAccess = false,
                    isLoading = false,
                    lastSyncedLabel = SyncLabelFormatter.formatLastSynced(syncMetadata?.lastSuccessAt),
                )
            } else {
                val analysis = usageInsightsAnalyzer.analyze(weekApps, weekSummaries, today)
                InsightsUiState(
                    hasUsageAccess = true,
                    isLoading = false,
                    weeklyScreenTimeHours = buildWeeklyChart(weekSummaries, weekStartKey, todayKey).map { it.value },
                    productivePercent = analysis.productivePercent,
                    neutralPercent = analysis.neutralPercent,
                    distractingPercent = analysis.distractingPercent,
                    behavioralInsights = analysis.behavioralInsights,
                    recommendations = analysis.recommendations,
                    lastSyncedLabel = SyncLabelFormatter.formatLastSynced(syncMetadata?.lastSuccessAt),
                )
            }
        }.distinctUntilChanged()
    }

    fun observeAppDetailsState(packageName: String): Flow<AppDetailsUiState> {
        val today = DateUtils.today()
        val todayKey = DateUtils.toDayKey(today)
        val weekStartKey = DateUtils.toDayKey(today.minusDays(6))

        return combine(
            appUsageDao.observeAppUsageHistory(packageName, weekStartKey, todayKey),
            appCategoryRepository.observeCategoryChanges(),
        ) { history, categoryOverrides ->
            if (history.isEmpty()) {
                return@combine AppDetailsUiState(isLoading = false, packageName = packageName)
            }
            
            val todayUsage = history.find { it.usageDate == todayKey }
            val appName = history.first().appName
            val categoryOverride = categoryOverrides.find { it.packageName == packageName }
            val category = categoryOverride?.let { AppCategory.fromStorageKey(it.category) } ?: AppCategory.Neutral

            val todayDurationLabel = todayUsage?.let { DurationFormatter.formatShort(it.usageDurationMs) } ?: "0m"
            
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
                todayDurationLabel = todayDurationLabel,
                todaySessionCount = 0,
                averageSessionLabel = "0m",
                weeklyUsageChart = chartPoints,
            )
        }.distinctUntilChanged()
    }

    fun observeProfileState(): Flow<ProfileUiState> =
        combine(
            dailySummaryDao.observeAllSummaries(),
            syncMetadataDao.observeMetadata(),
        ) { summaries, _ ->
            val hasAccess = usageAccessChecker.hasUsageAccess()
            if (!hasAccess) {
                ProfileUiState(hasUsageAccess = false, isLoading = false)
            } else {
                buildProfileStateFromSummaries(summaries)
            }
        }.distinctUntilChanged()

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
        recomputeDailySummary(DateUtils.today())
        return category
    }

    suspend fun setAppCategory(packageName: String, category: AppCategory) {
        appCategoryRepository.setUserCategory(packageName, category)
        appUsageDao.updateCategoryForPackage(packageName, category.storageKey)
        recomputeDailySummary(DateUtils.today())
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
        // Only fallback to apps if screen time is 0, but cap it at 24 hours to avoid overlapping inflation
        val maxMsInDay = 24 * 60 * 60 * 1000L
        val totalScreenMs = if (totalFromScreen > 0) totalFromScreen else totalFromApps.coerceAtMost(maxMsInDay)
        val topApp = appUsageDao.getTopApp(dayKey)
        val unlockCount = unlockEventDao.getUnlockCount(dayKey)
        val focusScore = dashboardMapper.computeFocusScore(totalScreenMs, topApps)

        dailySummaryDao.upsert(
            DailySummaryEntity(
                summaryDate = dayKey,
                totalScreenTimeMs = totalScreenMs,
                unlockCount = unlockCount,
                pickupCount = unlockCount,
                notificationCount = 0,
                topAppPackage = topApp?.packageName,
                topAppName = topApp?.appName,
                focusScore = focusScore,
                computedAt = now,
            ),
        )
    }

    private fun buildProfileStateFromSummaries(
        summaries: List<DailySummaryEntity>,
    ): ProfileUiState {
        val tracked = summaries.count { it.totalScreenTimeMs > 0 }
        val lifetimeMs = summaries.sumOf { it.totalScreenTimeMs }
        val avgFocus = summaries.filter { it.focusScore > 0 }
            .map { it.focusScore }
            .average()
            .toInt()
        val recent = summaries.take(14)
        val focusStreak = calculateFocusStreak(recent)
        val distractionReduction = calculateDistractionReduction(recent)

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
