package com.lumetrix.statsmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "unlock_events",
    indices = [
        Index(value = ["event_date"]),
        Index(value = ["timestamp_ms"]),
    ],
)
data class UnlockEventEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "timestamp_ms")
    val timestampMs: Long,
    /** e.g. pin, biometric, swipe — extensible without schema break. */
    @ColumnInfo(name = "unlock_type")
    val unlockType: String,
    @ColumnInfo(name = "event_date")
    val eventDate: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
