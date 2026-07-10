package com.lumetrix.statsmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * An App Chain Rule: block [targetPackage] unless [gatePackage] has been used
 * for at least [gateDurationMin] minutes today.
 */
@Entity(
    tableName = "app_chain_rules",
    indices = [Index(value = ["gate_package", "target_package"], unique = true)],
)
data class AppChainRuleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    /** Package that must be used first (the "gate"). */
    @ColumnInfo(name = "gate_package")
    val gatePackage: String,
    /** Friendly name of the gate app. */
    @ColumnInfo(name = "gate_app_name")
    val gateAppName: String,
    /** Minutes of gate-app usage required today to satisfy the rule. */
    @ColumnInfo(name = "gate_duration_min")
    val gateDurationMin: Int,
    /** Package that becomes accessible once the gate is satisfied. */
    @ColumnInfo(name = "target_package")
    val targetPackage: String,
    /** Friendly name of the target app. */
    @ColumnInfo(name = "target_app_name")
    val targetAppName: String,
    /** Whether this rule is currently enforced. */
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
