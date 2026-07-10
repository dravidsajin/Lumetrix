package com.lumetrix.statsmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lumetrix.statsmanager.data.local.entity.DailySummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(summary: DailySummaryEntity)

    @Query("SELECT * FROM daily_summaries WHERE summary_date = :dayKey LIMIT 1")
    fun observeSummary(dayKey: Int): Flow<DailySummaryEntity?>

    @Query(
        """
        SELECT * FROM daily_summaries
        WHERE summary_date BETWEEN :startDayKey AND :endDayKey
        ORDER BY summary_date ASC
        """,
    )
    fun observeSummariesBetween(startDayKey: Int, endDayKey: Int): Flow<List<DailySummaryEntity>>

    @Query(
        """
        SELECT * FROM daily_summaries
        WHERE summary_date BETWEEN :startDayKey AND :endDayKey
        ORDER BY summary_date ASC
        """,
    )
    suspend fun getSummariesBetween(startDayKey: Int, endDayKey: Int): List<DailySummaryEntity>

    @Query(
        """
        SELECT COUNT(*) FROM daily_summaries
        WHERE total_screen_time_ms > 0
        """,
    )
    suspend fun getTrackedDaysCount(): Int

    @Query("SELECT COALESCE(SUM(total_screen_time_ms), 0) FROM daily_summaries")
    suspend fun getLifetimeScreenTimeMs(): Long

    @Query(
        """
        SELECT AVG(focus_score) FROM daily_summaries
        WHERE focus_score > 0
        """,
    )
    suspend fun getAverageFocusScore(): Double?

    @Query(
        """
        SELECT * FROM daily_summaries
        ORDER BY summary_date DESC
        LIMIT :limit
        """,
    )
    suspend fun getRecentSummaries(limit: Int): List<DailySummaryEntity>

    @Query("SELECT * FROM daily_summaries ORDER BY summary_date DESC")
    fun observeAllSummaries(): Flow<List<DailySummaryEntity>>
}
