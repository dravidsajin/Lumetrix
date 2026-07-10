package com.lumetrix.statsmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * User or system-assigned category for an app.
 * User overrides always win over system defaults during sync.
 */
@Entity(
    tableName = "app_category_overrides",
    indices = [
        Index(value = ["package_name"], unique = true),
    ],
)
data class AppCategoryOverrideEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "source")
    val source: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
