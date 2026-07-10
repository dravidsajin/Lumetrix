package com.lumetrix.statsmanager.core.time

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object SyncLabelFormatter {

    private val formatter = DateTimeFormatter.ofPattern("h:mm a")
    private val zoneId = ZoneId.systemDefault()

    fun formatLastSynced(epochMillis: Long?): String? {
        if (epochMillis == null || epochMillis <= 0L) return null
        val time = Instant.ofEpochMilli(epochMillis).atZone(zoneId).format(formatter)
        return "Last synced $time"
    }
}
