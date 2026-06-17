package com.lumetrix.statsmanager.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lumetrix.statsmanager.ui.components.AmbientBackground
import com.lumetrix.statsmanager.ui.components.FloatingBottomBar
import com.lumetrix.statsmanager.ui.dashboard.DashboardScreen
import com.lumetrix.statsmanager.ui.focus.FocusScreen
import com.lumetrix.statsmanager.ui.insights.InsightsScreen
import com.lumetrix.statsmanager.ui.navigation.LumetrixDestination
import com.lumetrix.statsmanager.ui.navigation.indexFromRoute
import com.lumetrix.statsmanager.ui.navigation.lumetrixBottomNavItems
import com.lumetrix.statsmanager.ui.profile.ProfileScreen
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens

@Composable
fun LumetrixApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedIndex = indexFromRoute(currentRoute)

    Box(modifier = Modifier.fillMaxSize()) {
        AmbientBackground(modifier = Modifier.fillMaxSize())

        NavHost(
            navController = navController,
            startDestination = LumetrixDestination.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = LumetrixTokens.BottomNavHeight + LumetrixTokens.BottomNavBottomPadding),
        ) {
            composable(LumetrixDestination.Dashboard.route) {
                AnimatedScreen { DashboardScreen() }
            }
            composable(LumetrixDestination.Insights.route) {
                AnimatedScreen { InsightsScreen() }
            }
            composable(LumetrixDestination.Focus.route) {
                AnimatedScreen { FocusScreen() }
            }
            composable(LumetrixDestination.Profile.route) {
                AnimatedScreen { ProfileScreen() }
            }
        }

        FloatingBottomBar(
            items = lumetrixBottomNavItems(),
            selectedIndex = selectedIndex,
            onItemSelected = { index ->
                val destination = LumetrixDestination.entries[index]
                navController.navigate(destination.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(bottom = LumetrixTokens.BottomNavBottomPadding),
        )
    }
}

@Composable
private fun AnimatedScreen(content: @Composable () -> Unit) {
    AnimatedContent(
        targetState = Unit,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screenTransition",
    ) {
        content()
    }
}
