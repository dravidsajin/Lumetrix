package com.lumetrix.statsmanager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Settings
import com.lumetrix.statsmanager.ui.components.BottomNavItem

enum class LumetrixDestination(
    val route: String,
    val label: String,
) {
    Pulse("pulse", "Pulse"),
    Insights("insights", "Insights"),
    Focus("focus", "Focus"),
    Apps("apps", "Apps"),
    Settings("settings", "Settings")
}

fun lumetrixBottomNavItems(): List<BottomNavItem> = listOf(
    BottomNavItem(
        label = LumetrixDestination.Pulse.label,
        icon = Icons.Outlined.Dashboard,
        selectedIcon = Icons.Rounded.Dashboard,
    ),
    BottomNavItem(
        label = LumetrixDestination.Insights.label,
        icon = Icons.Outlined.Lightbulb,
        selectedIcon = Icons.Rounded.Lightbulb,
    ),
    BottomNavItem(
        label = LumetrixDestination.Focus.label,
        icon = Icons.Outlined.CenterFocusStrong,
        selectedIcon = Icons.Rounded.CenterFocusStrong,
    ),
    BottomNavItem(
        label = LumetrixDestination.Apps.label,
        icon = Icons.Outlined.Apps,
        selectedIcon = Icons.Rounded.Apps,
    ),
    BottomNavItem(
        label = LumetrixDestination.Settings.label,
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Rounded.Settings,
    ),
)

fun destinationFromIndex(index: Int): LumetrixDestination =
    LumetrixDestination.entries.getOrElse(index) { LumetrixDestination.Pulse }

fun indexFromRoute(route: String?): Int =
    LumetrixDestination.entries.indexOfFirst { it.route == route }.coerceAtLeast(0)
