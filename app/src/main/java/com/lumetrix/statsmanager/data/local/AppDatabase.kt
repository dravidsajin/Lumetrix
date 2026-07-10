package com.lumetrix.statsmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lumetrix.statsmanager.data.local.dao.AppCategoryDao
import com.lumetrix.statsmanager.data.local.dao.AppChainRuleDao
import com.lumetrix.statsmanager.data.local.dao.AppUsageDao
import com.lumetrix.statsmanager.data.local.dao.DailySummaryDao
import com.lumetrix.statsmanager.data.local.dao.FocusPointsDao
import com.lumetrix.statsmanager.data.local.dao.FocusSessionDao
import com.lumetrix.statsmanager.data.local.dao.ScreenSessionDao
import com.lumetrix.statsmanager.data.local.dao.SyncMetadataDao
import com.lumetrix.statsmanager.data.local.dao.UnlockEventDao
import com.lumetrix.statsmanager.data.local.entity.AppCategoryOverrideEntity
import com.lumetrix.statsmanager.data.local.entity.AppChainRuleEntity
import com.lumetrix.statsmanager.data.local.entity.AppUsageEntity
import com.lumetrix.statsmanager.data.local.entity.DailySummaryEntity
import com.lumetrix.statsmanager.data.local.entity.FocusPointsEntity
import com.lumetrix.statsmanager.data.local.entity.FocusSessionEntity
import com.lumetrix.statsmanager.data.local.entity.ScreenSessionEntity
import com.lumetrix.statsmanager.data.local.entity.SyncMetadataEntity
import com.lumetrix.statsmanager.data.local.entity.UnlockEventEntity
import com.lumetrix.statsmanager.data.local.migration.DatabaseMigrations

@Database(
    entities = [
        AppUsageEntity::class,
        ScreenSessionEntity::class,
        UnlockEventEntity::class,
        DailySummaryEntity::class,
        AppCategoryOverrideEntity::class,
        SyncMetadataEntity::class,
        FocusSessionEntity::class,
        FocusPointsEntity::class,
        AppChainRuleEntity::class,
    ],
    version = DatabaseMigrations.CURRENT_VERSION,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appUsageDao(): AppUsageDao

    abstract fun screenSessionDao(): ScreenSessionDao

    abstract fun unlockEventDao(): UnlockEventDao

    abstract fun dailySummaryDao(): DailySummaryDao

    abstract fun appCategoryDao(): AppCategoryDao

    abstract fun syncMetadataDao(): SyncMetadataDao

    abstract fun focusSessionDao(): FocusSessionDao

    abstract fun focusPointsDao(): FocusPointsDao

    abstract fun appChainRuleDao(): AppChainRuleDao
}

