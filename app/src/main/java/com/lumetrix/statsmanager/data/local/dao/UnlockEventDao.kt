package com.lumetrix.statsmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lumetrix.statsmanager.data.local.entity.UnlockEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockEventDao {

    @Insert
    suspend fun insert(event: UnlockEventEntity)

    @Query(
        """
        SELECT COUNT(*) FROM unlock_events
        WHERE event_date = :dayKey
        """,
    )
    fun observeUnlockCount(dayKey: Int): Flow<Int>

    @Query(
        """
        SELECT COUNT(*) FROM unlock_events
        WHERE event_date = :dayKey
        """,
    )
    suspend fun getUnlockCount(dayKey: Int): Int

    @Query("SELECT MAX(timestamp_ms) FROM unlock_events")
    suspend fun getLatestTimestamp(): Long?

    @Query(
        """
        SELECT COUNT(*) FROM unlock_events
        WHERE event_date BETWEEN :startDayKey AND :endDayKey
        """,
    )
    suspend fun getUnlockCountBetween(startDayKey: Int, endDayKey: Int): Int
}
