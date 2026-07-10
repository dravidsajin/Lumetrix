package com.lumetrix.statsmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Running ledger of focus points.
 * Each row is a delta (positive = earned, negative = spent).
 * Query the SUM(delta) for the current balance.
 */
@Entity(tableName = "focus_points")
data class FocusPointsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    /** Positive for earned, negative for spent. */
    @ColumnInfo(name = "delta")
    val delta: Int,
    /** Human-readable reason, e.g. "Completed 25m Deep Work" or "Spent to unlock Instagram". */
    @ColumnInfo(name = "reason")
    val reason: String,
    @ColumnInfo(name = "timestamp_ms")
    val timestampMs: Long,
)
