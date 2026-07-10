package com.lumetrix.statsmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "screen_sessions",
    indices = [
        Index(value = ["session_date"]),
        Index(value = ["screen_on_ms"]),
    ],
)
data class ScreenSessionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "screen_on_ms")
    val screenOnMs: Long,
    @ColumnInfo(name = "screen_off_ms")
    val screenOffMs: Long,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,
    @ColumnInfo(name = "session_date")
    val sessionDate: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
