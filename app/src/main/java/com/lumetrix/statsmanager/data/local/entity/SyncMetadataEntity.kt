package com.lumetrix.statsmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Single-row table (id = 1) tracking sync health. */
@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = SINGLETON_ID,
    @ColumnInfo(name = "last_success_at")
    val lastSuccessAt: Long?,
    @ColumnInfo(name = "last_attempt_at")
    val lastAttemptAt: Long,
    @ColumnInfo(name = "last_status")
    val lastStatus: String,
    @ColumnInfo(name = "last_error")
    val lastError: String?,
    @ColumnInfo(name = "days_synced")
    val daysSynced: Int,
) {
    companion object {
        const val SINGLETON_ID = 1
        const val STATUS_NEVER = "never"
        const val STATUS_SUCCESS = "success"
        const val STATUS_FAILED = "failed"
    }
}
