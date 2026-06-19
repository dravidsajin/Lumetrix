package com.lumetrix.statsmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lumetrix.statsmanager.data.local.entity.AppCategoryOverrideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppCategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(override: AppCategoryOverrideEntity)

    @Query("SELECT * FROM app_category_overrides WHERE package_name = :packageName LIMIT 1")
    suspend fun getByPackage(packageName: String): AppCategoryOverrideEntity?

    @Query("SELECT * FROM app_category_overrides")
    suspend fun getAll(): List<AppCategoryOverrideEntity>

    @Query("SELECT * FROM app_category_overrides")
    fun observeAll(): Flow<List<AppCategoryOverrideEntity>>
}
