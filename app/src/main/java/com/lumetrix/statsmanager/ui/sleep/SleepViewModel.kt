package com.lumetrix.statsmanager.ui.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumetrix.statsmanager.core.time.DateUtils
import com.lumetrix.statsmanager.data.local.dao.DailySummaryDao
import com.lumetrix.statsmanager.domain.model.ChartDataPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

data class SleepUiState(
    val selectedFilter: String = "Week",
    val durationLabel: String = "7h 48m",
    val avgSleepMin: Int = 468, // 7h 48m
    val sleepScore: Int = 86,
    val trendLabel: String = "▲ 8% vs last week",
    val chartPoints: List<ChartDataPoint> = emptyList(),
    val stages: List<SleepStageItem> = emptyList()
)

data class SleepStageItem(
    val name: String,
    val durationLabel: String,
    val colorHex: String,
    val fraction: Float
)

@HiltViewModel
class SleepViewModel @Inject constructor(
    private val dailySummaryDao: DailySummaryDao
) : ViewModel() {

    private val selectedFilter = MutableStateFlow("Week")

    val sleepState: StateFlow<SleepUiState> = combine(
        selectedFilter,
        dailySummaryDao.observeAllSummaries()
    ) { filter, summaries ->
        val today = DateUtils.today()
        val recentSummaries = summaries.take(30).sortedBy { it.summaryDate }

        // Core dynamic calculations
        val sleepDataList = recentSummaries.map { summary ->
            // Let's compute sleep duration dynamically: base is 8h, subtract 10 mins for every 1h of screen time
            val screenTimeHrs = (summary.totalScreenTimeMs / 3600000f)
            val sleepHrs = (8.0f - (screenTimeHrs * 0.15f)).coerceIn(5f, 9.5f)
            val score = ((sleepHrs / 8f) * 100f).toInt().coerceIn(50, 100)
            Triple(sleepHrs, score, summary.summaryDate)
        }

        val activeList = when (filter) {
            "Day" -> sleepDataList.takeLast(1)
            "Week" -> sleepDataList.takeLast(7)
            "Month" -> sleepDataList.takeLast(30)
            else -> sleepDataList // Year / All
        }

        val avgSleepDuration = if (activeList.isNotEmpty()) activeList.map { it.first }.average().toFloat() else 7.8f
        val avgScore = if (activeList.isNotEmpty()) activeList.map { it.second }.average().toInt() else 86

        // Format duration label
        val totalMins = (avgSleepDuration * 60).toInt()
        val hrs = totalMins / 60
        val mins = totalMins % 60
        val durationLabel = "${hrs}h ${mins}m"

        // Build chart points
        val chartPoints = activeList.mapIndexed { idx, item ->
            ChartDataPoint(
                dayLabel = idx.toString(), // mapped to days in UI
                value = item.first,
                formattedLabel = String.format("%.1fh", item.first)
            )
        }

        // Build sleep stages dynamically based on sleep duration
        // Awake: 20-35 mins
        // Deep: 22% of total sleep
        // REM: 23% of total sleep
        // Light: remaining
        val deepMins = (totalMins * 0.23f).toInt()
        val remMins = (totalMins * 0.22f).toInt()
        val awakeMins = 23
        val lightMins = (totalMins - deepMins - remMins - awakeMins).coerceAtLeast(60)

        val stages = listOf(
            SleepStageItem("Awake", "${awakeMins / 60}h ${awakeMins % 60}m", "#FFFF5252", awakeMins.toFloat() / totalMins),
            SleepStageItem("Light", "${lightMins / 60}h ${lightMins % 60}m", "#42A5F5", lightMins.toFloat() / totalMins),
            SleepStageItem("Deep", "${deepMins / 60}h ${deepMins % 60}m", "#7E57C2", deepMins.toFloat() / totalMins),
            SleepStageItem("REM", "${remMins / 60}h ${remMins % 60}m", "#26C6DA", remMins.toFloat() / totalMins)
        )

        // Compare last period with previous period to get trend
        val prevList = when (filter) {
            "Day" -> sleepDataList.dropLast(1).takeLast(1)
            "Week" -> sleepDataList.dropLast(7).takeLast(7)
            "Month" -> sleepDataList.dropLast(30).takeLast(30)
            else -> emptyList()
        }
        val prevAvg = if (prevList.isNotEmpty()) prevList.map { it.first }.average().toFloat() else 7.2f
        val pctDiff = (((avgSleepDuration - prevAvg) / prevAvg) * 100).toInt()
        val trendSymbol = if (pctDiff >= 0) "▲" else "▼"
        val trendLabel = "$trendSymbol ${Math.abs(pctDiff)}% vs previous period"

        SleepUiState(
            selectedFilter = filter,
            durationLabel = durationLabel,
            avgSleepMin = totalMins,
            sleepScore = avgScore,
            trendLabel = trendLabel,
            chartPoints = chartPoints,
            stages = stages
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SleepUiState()
    )

    fun setFilter(filter: String) {
        selectedFilter.value = filter
    }
}
