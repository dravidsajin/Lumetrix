package com.lumetrix.statsmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Pre-computed daily rollup for fast dashboard reads.
 * Recomputed after each sync — never loses raw event/session rows.
 */
@Entity(
    tableName = "daily_summaries",
    indices = [
        Index(value = ["summary_date"], unique = true),
    ],
)
data class DailySummaryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "summary_date")
    val summaryDate: Int,
    @ColumnInfo(name = "total_screen_time_ms")
    val totalScreenTimeMs: Long,
    @ColumnInfo(name = "unlock_count")
    val unlockCount: Int,
    @ColumnInfo(name = "pickup_count")
    val pickupCount: Int,
    @ColumnInfo(name = "notification_count")
    val notificationCount: Int,
    @ColumnInfo(name = "top_app_package")
    val topAppPackage: String?,
    @ColumnInfo(name = "top_app_name")
    val topAppName: String?,
    @ColumnInfo(name = "focus_score")
    val focusScore: Int,
    @ColumnInfo(name = "computed_at")
    val computedAt: Long,
)
