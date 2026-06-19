package com.lumetrix.statsmanager.data.tracking

import android.app.Activity
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageAccessChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Triggers a usage-stats query so the OS registers this app in the Usage Access list
     * (required on some OEM skins before Lumetrix appears in settings).
     */
    fun registerForUsageAccessList() {
        runCatching {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE)
                as UsageStatsManager
            val end = System.currentTimeMillis()
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, end - 1, end)
        }
    }

    /**
     * Opens the Usage Access toggle for Lumetrix when possible, with OEM fallbacks.
     */
    fun usageAccessSettingsIntent(): Intent = buildSettingsIntent()

    fun launchUsageAccessSettings(context: Context) {
        registerForUsageAccessList()
        val intent = buildSettingsIntent()
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun buildSettingsIntent(): Intent {
        val packageUri = Uri.parse("package:${context.packageName}")
        val candidates = listOf(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                data = packageUri
            },
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = packageUri
            },
        )

        for (candidate in candidates) {
            if (candidate.resolveActivity(context.packageManager) != null) {
                return candidate
            }
        }

        return Intent(Settings.ACTION_SETTINGS)
    }
}
