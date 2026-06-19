package com.lumetrix.statsmanager.core.time

import java.time.LocalTime

object GreetingUtils {

    fun greetingForNow(): String {
        val hour = LocalTime.now().hour
        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }
}
