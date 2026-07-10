package com.lumetrix.statsmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lumetrix.statsmanager.data.local.entity.FocusPointsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusPointsDao {

    @Insert
    suspend fun insert(entry: FocusPointsEntity)

    /** Live balance = SUM of all deltas. */
    @Query("SELECT COALESCE(SUM(delta), 0) FROM focus_points")
    fun observeBalance(): Flow<Int>

    @Query("SELECT COALESCE(SUM(delta), 0) FROM focus_points")
    suspend fun getBalance(): Int

    /** Total points ever earned (positive deltas only). */
    @Query("SELECT COALESCE(SUM(delta), 0) FROM focus_points WHERE delta > 0")
    suspend fun getTotalEarned(): Int

    @Query("SELECT * FROM focus_points ORDER BY timestamp_ms DESC LIMIT :limit")
    suspend fun getRecentEntries(limit: Int = 20): List<FocusPointsEntity>
}
