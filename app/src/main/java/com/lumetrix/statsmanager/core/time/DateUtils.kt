package com.lumetrix.statsmanager.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val dayKeyFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    fun today(): LocalDate = LocalDate.now(zoneId)

    fun toDayKey(date: LocalDate): Int = date.format(dayKeyFormatter).toInt()

    fun fromDayKey(dayKey: Int): LocalDate =
        LocalDate.parse(dayKey.toString(), dayKeyFormatter)

    fun dayStartMillis(date: LocalDate): Long =
        date.atStartOfDay(zoneId).toInstant().toEpochMilli()

    fun dayEndMillis(date: LocalDate): Long =
        date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

    fun millisToLocalDate(epochMillis: Long): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate()

    fun lastNDays(endDate: LocalDate = today(), count: Int): List<LocalDate> =
        (count - 1 downTo 0).map { endDate.minusDays(it.toLong()) }
}
