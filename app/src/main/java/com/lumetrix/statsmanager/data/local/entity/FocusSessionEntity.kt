package com.lumetrix.statsmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persisted record of a single focus session.
 * Written by FocusViewModel when a session ends (completed or abandoned).
 */
@Entity(
    tableName = "focus_sessions",
    indices = [Index(value = ["session_date"])],
)
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    /** Epoch ms when the session started. */
    @ColumnInfo(name = "start_time_ms")
    val startTimeMs: Long,
    /** Epoch ms when the session ended (completed or abandoned). */
    @ColumnInfo(name = "end_time_ms")
    val endTimeMs: Long,
    /** "Deep Work" | "Study" | "Reading" | "Workout" */
    @ColumnInfo(name = "mode")
    val mode: String,
    /** Originally planned duration in minutes. */
    @ColumnInfo(name = "planned_duration_min")
    val plannedDurationMin: Int,
    /** True if the timer ran to 0; false if the user tapped Give Up. */
    @ColumnInfo(name = "was_completed")
    val wasCompleted: Boolean,
    /** Focus points earned for this session. */
    @ColumnInfo(name = "points_earned")
    val pointsEarned: Int,
    /** yyyyMMdd key for fast per-day queries. */
    @ColumnInfo(name = "session_date")
    val sessionDate: Int,
)
