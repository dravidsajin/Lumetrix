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

    const val CURRENT_VERSION = 3

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

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Feature 3: Ghost Pickup Detector
            db.execSQL(
                """
                ALTER TABLE daily_summaries
                ADD COLUMN ghost_pickups INTEGER NOT NULL DEFAULT 0
                """.trimIndent(),
            )

            // Feature 7: Focus Session History (Pomodoro Stats)
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS focus_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    start_time_ms INTEGER NOT NULL,
                    end_time_ms INTEGER NOT NULL,
                    mode TEXT NOT NULL,
                    planned_duration_min INTEGER NOT NULL,
                    was_completed INTEGER NOT NULL,
                    points_earned INTEGER NOT NULL,
                    session_date INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_focus_sessions_session_date
                ON focus_sessions (session_date)
                """.trimIndent(),
            )

            // Feature 5: Focus Points Economy
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS focus_points (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    delta INTEGER NOT NULL,
                    reason TEXT NOT NULL,
                    timestamp_ms INTEGER NOT NULL
                )
                """.trimIndent(),
            )

            // Feature 6: App Chaining Rules
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS app_chain_rules (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    gate_package TEXT NOT NULL,
                    gate_app_name TEXT NOT NULL,
                    gate_duration_min INTEGER NOT NULL,
                    target_package TEXT NOT NULL,
                    target_app_name TEXT NOT NULL,
                    is_enabled INTEGER NOT NULL DEFAULT 1,
                    created_at INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS index_app_chain_rules_gate_target
                ON app_chain_rules (gate_package, target_package)
                """.trimIndent(),
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
    )
}

