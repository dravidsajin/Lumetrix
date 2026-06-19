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
            appUsageDao.observeTopApps(todayKey, limit = 5),
            appUsageDao.observeTotalUsageMs(todayKey),
            dailySummaryDao.observeSummary(todayKey),
            dailySummaryDao.observeSummariesBetween(weekStartKey, todayKey),
            unlockEventDao.observeUnlockCount(todayKey),
        ) { topApps, totalMs, summary, weekSummaries, unlockCount ->
            DashboardSnapshot(
                topApps = topApps,
                totalMs = totalMs,
                summary = summary,
                weekSummaries = weekSummaries,
                unlockCount = unlockCount,
            )
        }

        return combine(dashboardData, syncMetadataDao.observeMetadata()) { snapshot, syncMetadata ->
            val hasAccess = usageAccessChecker.hasUsageAccess()
            val focusScore = snapshot.summary?.focusScore
                ?: dashboardMapper.computeFocusScore(snapshot.totalMs, snapshot.topApps)
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
                topApps = if (hasAccess) dashboardMapper.toAppUsageItems(snapshot.topApps) else emptyList(),
                unlockCount = if (hasAccess) snapshot.summary?.unlockCount ?: snapshot.unlockCount else 0,
                notificationCount = if (hasAccess) snapshot.summary?.notificationCount ?: 0 else 0,
                pickupCount = if (hasAccess) snapshot.summary?.pickupCount ?: snapshot.unlockCount else 0,
                focusTimeLabel = if (hasAccess) DurationFormatter.formatShort(snapshot.totalMs) else "—",
                totalScreenTimeLabel = if (hasAccess) DurationFormatter.formatShort(snapshot.totalMs) else "—",
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
                    weeklyScreenTimeHours = buildWeeklyChart(weekSummaries, weekStartKey, todayKey),
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
        val totalScreenMs = maxOf(totalFromApps, totalFromScreen)
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
    ): List<Float> {
        val byDay = summaries.associateBy { it.summaryDate }
        return DateUtils.lastNDays(DateUtils.fromDayKey(endKey), count = 7).map { date ->
            val key = DateUtils.toDayKey(date)
            DurationFormatter.formatHoursForChart(byDay[key]?.totalScreenTimeMs ?: 0L)
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

private data class DashboardSnapshot(
    val topApps: List<AppUsageEntity>,
    val totalMs: Long,
    val summary: DailySummaryEntity?,
    val weekSummaries: List<DailySummaryEntity>,
    val unlockCount: Int,
)
