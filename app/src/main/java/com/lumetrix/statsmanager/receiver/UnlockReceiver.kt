package com.lumetrix.statsmanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lumetrix.statsmanager.data.repository.UsageTrackingRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UnlockReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: UsageTrackingRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_USER_PRESENT) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.recordUnlock()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
