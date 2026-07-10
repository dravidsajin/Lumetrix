package com.lumetrix.statsmanager.di

import android.content.Context
import androidx.room.Room
import com.lumetrix.statsmanager.data.local.AppDatabase
import com.lumetrix.statsmanager.data.local.dao.AppCategoryDao
import com.lumetrix.statsmanager.data.local.dao.AppChainRuleDao
import com.lumetrix.statsmanager.data.local.dao.AppUsageDao
import com.lumetrix.statsmanager.data.local.dao.DailySummaryDao
import com.lumetrix.statsmanager.data.local.dao.FocusPointsDao
import com.lumetrix.statsmanager.data.local.dao.FocusSessionDao
import com.lumetrix.statsmanager.data.local.dao.ScreenSessionDao
import com.lumetrix.statsmanager.data.local.dao.SyncMetadataDao
import com.lumetrix.statsmanager.data.local.dao.UnlockEventDao
import com.lumetrix.statsmanager.data.local.migration.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "lumetrix.db"

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        DATABASE_NAME,
    )
        .addMigrations(*DatabaseMigrations.ALL)
        .build()

    @Provides
    fun provideAppUsageDao(database: AppDatabase): AppUsageDao = database.appUsageDao()

    @Provides
    fun provideScreenSessionDao(database: AppDatabase): ScreenSessionDao =
        database.screenSessionDao()

    @Provides
    fun provideUnlockEventDao(database: AppDatabase): UnlockEventDao =
        database.unlockEventDao()

    @Provides
    fun provideDailySummaryDao(database: AppDatabase): DailySummaryDao =
        database.dailySummaryDao()

    @Provides
    fun provideAppCategoryDao(database: AppDatabase): AppCategoryDao =
        database.appCategoryDao()

    @Provides
    fun provideSyncMetadataDao(database: AppDatabase): SyncMetadataDao =
        database.syncMetadataDao()

    @Provides
    fun provideFocusSessionDao(database: AppDatabase): FocusSessionDao =
        database.focusSessionDao()

    @Provides
    fun provideFocusPointsDao(database: AppDatabase): FocusPointsDao =
        database.focusPointsDao()

    @Provides
    fun provideAppChainRuleDao(database: AppDatabase): AppChainRuleDao =
        database.appChainRuleDao()
}

