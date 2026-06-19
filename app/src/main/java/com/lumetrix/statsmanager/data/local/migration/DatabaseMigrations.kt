package com.lumetrix.statsmanager.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Central registry for Room migrations.
 *
 * Rules for future schema changes:
 * - Never use fallbackToDestructiveMigration() in production builds.
 * - Add a new Migration(n, n+1) for every version bump.
 * - Prefer ADD COLUMN with DEFAULT for new fields (preserves existing rows).
 * - Commit exported JSON schemas under app/schemas/ after each change.
 */
object DatabaseMigrations {

    const val CURRENT_VERSION = 2

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                ALTER TABLE app_usage_records
                ADD COLUMN category TEXT NOT NULL DEFAULT 'neutral'
                """.trimIndent(),
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS app_category_overrides (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    package_name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    source TEXT NOT NULL,
                    updated_at INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS index_app_category_overrides_package_name
                ON app_category_overrides (package_name)
                """.trimIndent(),
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS sync_metadata (
                    id INTEGER PRIMARY KEY NOT NULL,
                    last_success_at INTEGER,
                    last_attempt_at INTEGER NOT NULL,
                    last_status TEXT NOT NULL,
                    last_error TEXT,
                    days_synced INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO sync_metadata (id, last_success_at, last_attempt_at, last_status, last_error, days_synced)
                VALUES (1, NULL, 0, 'never', NULL, 0)
                """.trimIndent(),
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(
        MIGRATION_1_2,
    )
}
