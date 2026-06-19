package com.lumetrix.statsmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Daily per-app usage aggregate.
 *
 * One row per (package_name, usage_date). Safe to upsert on every sync without duplicating data.
 */
@Entity(
    tableName = "app_usage_records",
    indices = [
        Index(value = ["usage_date"]),
        Index(value = ["package_name", "usage_date"], unique = true),
        Index(value = ["usage_date", "usage_duration_ms"]),
    ],
)
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "app_name")
    val appName: String,
    @ColumnInfo(name = "usage_duration_ms")
    val usageDurationMs: Long,
    @ColumnInfo(name = "session_count")
    val sessionCount: Int,
    /** Stored as yyyyMMdd for compact indexed lookups. */
    @ColumnInfo(name = "usage_date")
    val usageDate: Int,
    /** productive | neutral | distracting — denormalized for fast reads. */
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
