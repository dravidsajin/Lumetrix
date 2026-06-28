package com.lumetrix.statsmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lumetrix.statsmanager.data.local.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(records: List<AppUsageEntity>)

    @Query(
        """
        SELECT * FROM app_usage_records
        WHERE usage_date = :dayKey
        ORDER BY usage_duration_ms DESC
        LIMIT :limit
        """,
    )
    fun observeTopApps(dayKey: Int, limit: Int = 10): Flow<List<AppUsageEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(usage_duration_ms), 0)
        FROM app_usage_records
        WHERE usage_date = :dayKey
        """,
    )
    fun observeTotalUsageMs(dayKey: Int): Flow<Long>

    @Query(
        """
        SELECT * FROM app_usage_records
        WHERE usage_date BETWEEN :startDayKey AND :endDayKey
        ORDER BY usage_date ASC, usage_duration_ms DESC
        """,
    )
    suspend fun getUsageBetween(startDayKey: Int, endDayKey: Int): List<AppUsageEntity>

    @Query(
        """
        SELECT COALESCE(SUM(usage_duration_ms), 0)
        FROM app_usage_records
        WHERE usage_date = :dayKey
        """,
    )
    suspend fun getTotalUsageMs(dayKey: Int): Long

    @Query(
        """
        UPDATE app_usage_records
        SET category = :category
        WHERE package_name = :packageName
        """,
    )
    suspend fun updateCategoryForPackage(packageName: String, category: String)

    @Query(
        """
        SELECT * FROM app_usage_records
        WHERE usage_date BETWEEN :startDayKey AND :endDayKey
        """,
    )
    fun observeAllUsageBetween(startDayKey: Int, endDayKey: Int): Flow<List<AppUsageEntity>>

    @Query(
        """
        SELECT * FROM app_usage_records
        WHERE usage_date = :dayKey
        ORDER BY usage_duration_ms DESC
        LIMIT 1
        """,
    )
    suspend fun getTopApp(dayKey: Int): AppUsageEntity?

    @Query(
        """
        SELECT * FROM app_usage_records
        WHERE package_name = :packageName AND usage_date BETWEEN :startDayKey AND :endDayKey
        ORDER BY usage_date ASC
        """,
    )
    fun observeAppUsageHistory(packageName: String, startDayKey: Int, endDayKey: Int): Flow<List<AppUsageEntity>>
}
