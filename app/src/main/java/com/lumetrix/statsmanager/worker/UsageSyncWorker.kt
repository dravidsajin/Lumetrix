package com.lumetrix.statsmanager.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lumetrix.statsmanager.data.repository.UsageTrackingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class UsageSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: UsageTrackingRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        runCatching {
            repository.syncRecentDays(dayCount = 7)
            Result.success()
        }.getOrElse {
            Result.retry()
        }

    companion object {
        const val WORK_NAME = "usage_sync_worker"
    }
}
