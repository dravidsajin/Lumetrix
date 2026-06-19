package com.lumetrix.statsmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lumetrix.statsmanager.data.local.entity.ScreenSessionEntity

@Dao
interface ScreenSessionDao {

    @Insert
    suspend fun insertAll(sessions: List<ScreenSessionEntity>)

    @Query("DELETE FROM screen_sessions WHERE session_date = :dayKey")
    suspend fun deleteForDate(dayKey: Int)

    @Query(
        """
        SELECT COALESCE(SUM(duration_ms), 0)
        FROM screen_sessions
        WHERE session_date = :dayKey
        """,
    )
    suspend fun getTotalScreenTimeMs(dayKey: Int): Long
}
