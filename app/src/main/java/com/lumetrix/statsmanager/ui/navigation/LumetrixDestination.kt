package com.lumetrix.statsmanager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.lumetrix.statsmanager.ui.components.BottomNavItem

enum class LumetrixDestination(
    val route: String,
    val label: String,
) {
    Dashboard("dashboard", "Dashboard"),
    Insights("insights", "Insights"),
    Focus("focus", "Focus"),
    Profile("profile", "Profile"),
}

fun lumetrixBottomNavItems(): List<BottomNavItem> = listOf(
    BottomNavItem(
        label = LumetrixDestination.Dashboard.label,
        icon = Icons.Outlined.Dashboard,
        selectedIcon = Icons.Rounded.Dashboard,
    ),
    BottomNavItem(
        label = LumetrixDestination.Insights.label,
        icon = Icons.Outlined.AutoGraph,
        selectedIcon = Icons.Rounded.AutoGraph,
    ),
    BottomNavItem(
        label = LumetrixDestination.Focus.label,
        icon = Icons.Outlined.CenterFocusStrong,
        selectedIcon = Icons.Rounded.CenterFocusStrong,
    ),
    BottomNavItem(
        label = LumetrixDestination.Profile.label,
        icon = Icons.Outlined.Person,
        selectedIcon = Icons.Rounded.Person,
    ),
)

fun destinationFromIndex(index: Int): LumetrixDestination =
    LumetrixDestination.entries.getOrElse(index) { LumetrixDestination.Dashboard }

fun indexFromRoute(route: String?): Int =
    LumetrixDestination.entries.indexOfFirst { it.route == route }.coerceAtLeast(0)
