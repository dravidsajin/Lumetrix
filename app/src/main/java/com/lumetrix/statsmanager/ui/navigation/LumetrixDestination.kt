package com.lumetrix.statsmanager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Person
import com.lumetrix.statsmanager.ui.components.BottomNavItem

enum class LumetrixDestination(
    val route: String,
    val label: String,
) {
    Pulse("pulse", "Pulse"),
    Apps("apps", "Apps"),
    Focus("focus", "Focus"),
    Insights("insights", "Insights"),
    Profile("profile", "Profile")
}

fun lumetrixBottomNavItems(): List<BottomNavItem> = listOf(
    BottomNavItem(
        label = LumetrixDestination.Pulse.label,
        icon = Icons.Outlined.Dashboard,
        selectedIcon = Icons.Rounded.Dashboard,
    ),
    BottomNavItem(
        label = LumetrixDestination.Apps.label,
        icon = Icons.Outlined.Apps,
        selectedIcon = Icons.Rounded.Apps,
    ),
    BottomNavItem(
        label = LumetrixDestination.Focus.label,
        icon = Icons.Outlined.CenterFocusStrong,
        selectedIcon = Icons.Rounded.CenterFocusStrong,
    ),
    BottomNavItem(
        label = LumetrixDestination.Insights.label,
        icon = Icons.Outlined.Lightbulb,
        selectedIcon = Icons.Rounded.Lightbulb,
    ),
    BottomNavItem(
        label = LumetrixDestination.Profile.label,
        icon = Icons.Outlined.Person,
        selectedIcon = Icons.Rounded.Person,
    ),
)

fun destinationFromIndex(index: Int): LumetrixDestination =
    LumetrixDestination.entries.getOrElse(index) { LumetrixDestination.Pulse }

fun indexFromRoute(route: String?): Int =
    LumetrixDestination.entries.indexOfFirst { it.route == route }.coerceAtLeast(0)
