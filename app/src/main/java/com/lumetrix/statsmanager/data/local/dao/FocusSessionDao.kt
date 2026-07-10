package com.lumetrix.statsmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lumetrix.statsmanager.data.local.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Insert
    suspend fun insert(session: FocusSessionEntity): Long

    @Query("SELECT * FROM focus_sessions WHERE session_date = :dayKey ORDER BY start_time_ms DESC")
    fun observeSessionsForDate(dayKey: Int): Flow<List<FocusSessionEntity>>

    @Query(
        """
        SELECT * FROM focus_sessions
        WHERE session_date BETWEEN :startDayKey AND :endDayKey
        ORDER BY session_date ASC, start_time_ms ASC
        """,
    )
    fun observeSessionsBetween(startDayKey: Int, endDayKey: Int): Flow<List<FocusSessionEntity>>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE session_date BETWEEN :startDayKey AND :endDayKey")
    suspend fun getSessionCount(startDayKey: Int, endDayKey: Int): Int

    @Query(
        """
        SELECT COUNT(*) FROM focus_sessions
        WHERE session_date BETWEEN :startDayKey AND :endDayKey AND was_completed = 1
        """,
    )
    suspend fun getCompletedSessionCount(startDayKey: Int, endDayKey: Int): Int

    @Query(
        """
        SELECT COALESCE(AVG(end_time_ms - start_time_ms), 0) FROM focus_sessions
        WHERE session_date BETWEEN :startDayKey AND :endDayKey AND was_completed = 1
        """,
    )
    suspend fun getAvgCompletedDurationMs(startDayKey: Int, endDayKey: Int): Long
}
