package com.lumetrix.statsmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lumetrix.statsmanager.data.local.entity.AppChainRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppChainRuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: AppChainRuleEntity): Long

    @Update
    suspend fun update(rule: AppChainRuleEntity)

    @Query("DELETE FROM app_chain_rules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM app_chain_rules ORDER BY created_at DESC")
    fun observeAllRules(): Flow<List<AppChainRuleEntity>>

    @Query("SELECT * FROM app_chain_rules WHERE is_enabled = 1")
    suspend fun getEnabledRules(): List<AppChainRuleEntity>

    /** Returns the rule that gates [targetPackage], if any. */
    @Query("SELECT * FROM app_chain_rules WHERE target_package = :targetPackage AND is_enabled = 1 LIMIT 1")
    suspend fun getRuleForTarget(targetPackage: String): AppChainRuleEntity?
}
