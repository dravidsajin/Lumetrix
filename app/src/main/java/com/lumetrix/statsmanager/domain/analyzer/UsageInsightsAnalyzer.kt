package com.lumetrix.statsmanager.domain.analyzer

import com.lumetrix.statsmanager.core.time.DateUtils
import com.lumetrix.statsmanager.core.time.DurationFormatter
import com.lumetrix.statsmanager.data.local.entity.AppUsageEntity
import com.lumetrix.statsmanager.data.local.entity.DailySummaryEntity
import com.lumetrix.statsmanager.data.tracking.UsagePatternAnalyzer
import com.lumetrix.statsmanager.domain.mapper.DashboardMapper
import com.lumetrix.statsmanager.domain.model.AppCategory
import com.lumetrix.statsmanager.domain.model.BehavioralInsightItem
import com.lumetrix.statsmanager.domain.model.InsightIcon
import com.lumetrix.statsmanager.domain.model.RecommendationItem
import java.time.LocalDate
import javax.inject.Inject

class UsageInsightsAnalyzer @Inject constructor(
    private val dashboardMapper: DashboardMapper,
    private val usagePatternAnalyzer: UsagePatternAnalyzer,
) {

    data class InsightsAnalysis(
        val productivePercent: Int,
        val neutralPercent: Int,
        val distractingPercent: Int,
        val behavioralInsights: List<BehavioralInsightItem>,
        val recommendations: List<RecommendationItem>,
        val periodUsage: com.lumetrix.statsmanager.domain.model.PeriodUsage,
        val todayPeriodUsage: com.lumetrix.statsmanager.domain.model.PeriodUsage,
    )

    fun analyze(
        weekApps: List<AppUsageEntity>,
        weekSummaries: List<DailySummaryEntity>,
        today: LocalDate,
    ): InsightsAnalysis {
        val (productive, neutral, distracting) = dashboardMapper.computeCategoryBreakdown(weekApps)
        val patterns = usagePatternAnalyzer.analyze(today)


        val insights = buildList {
            if (patterns.lateNightMsThisWeek > 0) {
                val change = patterns.lateNightChangePercent
                val subtitle = if (change > 0) {
                    "Late-night usage increased ${change}% compared to last week"
                } else if (change < 0) {
                    "Late-night usage decreased ${-change}% compared to last week"
                } else {
                    "Late-night usage is stable compared to last week"
                }
                add(
                    BehavioralInsightItem(
                        title = "You use your phone more after 10 PM",
                        subtitle = subtitle,
                        iconKey = InsightIcon.Night,
                    ),
                )
            }

            patterns.peakDistractionHour?.let { hour ->
                val formattedHour = formatHour(hour)
                add(
                    BehavioralInsightItem(
                        title = "Peak activity detected at $formattedHour",
                        subtitle = "Consider scheduling focus time away from this window",
                        iconKey = InsightIcon.Bedtime,
                    ),
                )
            }

            val avgUnlocks = weekSummaries.map { it.unlockCount }.average()
            if (avgUnlocks >= 40) {
                add(
                    BehavioralInsightItem(
                        title = "Frequent device pickups",
                        subtitle = "You averaged ${avgUnlocks.toInt()} unlocks per day this week",
                        iconKey = InsightIcon.Trending,
                    ),
                )
            }
        }

        val recommendations = buildList {
            val topDistracting = weekApps
                .filter { AppCategory.fromStorageKey(it.category) == AppCategory.Distracting }
                .maxByOrNull { it.usageDurationMs }

            topDistracting?.let { app ->
                add(
                    RecommendationItem(
                        title = "Reduce ${app.appName} time",
                        body = "You spent ${DurationFormatter.formatShort(app.usageDurationMs)} on ${app.appName} this week. Try a 45-minute daily limit.",
                    ),
                )
            }

            if (productive < distracting) {
                add(
                    RecommendationItem(
                        title = "Rebalance your screen time",
                        body = "Distracting apps outweigh productive ones this week. Block the top offender during focus hours.",
                    ),
                )
            }

            add(
                RecommendationItem(
                    title = "Optimize focus hours",
                    body = "Schedule deep work in your lowest-distraction window, typically mid-morning.",
                ),
            )
        }

        val totalMs = (productive + neutral + distracting).coerceAtLeast(1L)
        val prodPercent = (productive * 100 / totalMs).toInt()
        val neutPercent = (neutral * 100 / totalMs).toInt()
        val distPercent = (distracting * 100 / totalMs).toInt()

        return InsightsAnalysis(
            productivePercent = prodPercent,
            neutralPercent = neutPercent,
            distractingPercent = distPercent,
            behavioralInsights = insights.ifEmpty {
                listOf(
                    BehavioralInsightItem(
                        title = "Building your baseline",
                        subtitle = "Keep using your device normally — insights improve as more data is collected.",
                        iconKey = InsightIcon.Trending,
                    ),
                )
            },
            recommendations = recommendations,
            periodUsage = patterns.periodUsage,
            todayPeriodUsage = patterns.todayPeriodUsage,
        )
    }

    private fun formatHour(hour: Int): String {
        val normalized = hour % 24
        val display = when {
            normalized == 0 -> 12
            normalized > 12 -> normalized - 12
            else -> normalized
        }
        val suffix = if (normalized >= 12) "PM" else "AM"
        return "$display:00 $suffix"
    }
}
