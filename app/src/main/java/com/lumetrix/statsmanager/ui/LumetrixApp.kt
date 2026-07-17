package com.lumetrix.statsmanager.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.lumetrix.statsmanager.ui.appdetails.AppDetailsScreen
import com.lumetrix.statsmanager.ui.apps.AppsScreen
import com.lumetrix.statsmanager.ui.components.AmbientBackground
import com.lumetrix.statsmanager.ui.components.FloatingBottomBar
import com.lumetrix.statsmanager.ui.dashboard.DashboardScreen
import com.lumetrix.statsmanager.ui.focus.FocusScreen
import com.lumetrix.statsmanager.ui.insights.InsightsScreen
import com.lumetrix.statsmanager.ui.settings.SettingsScreen
import com.lumetrix.statsmanager.ui.goals.GoalsScreen
import com.lumetrix.statsmanager.ui.sleep.SleepScreen
import com.lumetrix.statsmanager.ui.wellness.WellnessScreen
import com.lumetrix.statsmanager.ui.report.WeeklyReportScreen
import com.lumetrix.statsmanager.ui.navigation.LumetrixDestination
import com.lumetrix.statsmanager.ui.navigation.lumetrixBottomNavItems
import com.lumetrix.statsmanager.ui.navigation.lumetrixTabTransition
import com.lumetrix.statsmanager.ui.theme.LumetrixMotion
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens

@Composable
fun LumetrixApp() {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    var appDetailsPackageName by rememberSaveable { mutableStateOf<String?>(null) }
    var activeOverlayScreen by rememberSaveable { mutableStateOf<String?>(null) }

    val systemNavInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomBarReserve = LumetrixTokens.BottomNavHeight +
        LumetrixTokens.BottomNavBottomPadding +
        systemNavInset

    Box(modifier = Modifier.fillMaxSize()) {
        AmbientBackground(modifier = Modifier.fillMaxSize())

        AnimatedContent(
            targetState = appDetailsPackageName ?: activeOverlayScreen,
            label = "overlayRouteTransition"
        ) { targetState ->
            when {
                appDetailsPackageName != null -> {
                    AppDetailsScreen(
                        packageName = appDetailsPackageName!!,
                        onNavigateBack = { appDetailsPackageName = null }
                    )
                }
                targetState == "goals" -> {
                    GoalsScreen(
                        onBack = { activeOverlayScreen = null }
                    )
                }
                targetState == "sleep" -> {
                    SleepScreen(
                        onBack = { activeOverlayScreen = null }
                    )
                }
                targetState == "wellness" -> {
                    WellnessScreen(
                        onBack = { activeOverlayScreen = null }
                    )
                }
                targetState == "report" -> {
                    WeeklyReportScreen(
                        onBack = { activeOverlayScreen = null }
                    )
                }
                targetState == "apps" -> {
                    AppsScreen(
                        onNavigateToAppDetails = { appDetailsPackageName = it },
                        onBack = { activeOverlayScreen = null }
                    )
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AnimatedContent(
                            targetState = selectedIndex,
                            transitionSpec = { lumetrixTabTransition() },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = bottomBarReserve),
                            label = "mainTabTransition",
                        ) { index ->
                            when (LumetrixDestination.entries[index]) {
                                LumetrixDestination.Pulse -> DashboardScreen(
                                    onNavigateToAppDetails = { appDetailsPackageName = it },
                                    onNavigateToSleep = { activeOverlayScreen = "sleep" },
                                    onNavigateToApps = { activeOverlayScreen = "apps" }
                                )
                                LumetrixDestination.Insights -> InsightsScreen(
                                    onNavigateToApps = { activeOverlayScreen = "apps" }
                                )
                                LumetrixDestination.Focus -> FocusScreen()
                                LumetrixDestination.Apps -> WeeklyReportScreen(
                                    onBack = null
                                )
                                LumetrixDestination.Settings -> SettingsScreen(
                                    onNavigateToOverlay = { activeOverlayScreen = it }
                                )
                            }
                        }

                        FloatingBottomBar(
                            items = lumetrixBottomNavItems(),
                            selectedIndex = selectedIndex,
                            onItemSelected = { selectedIndex = it },
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LumetrixAnimatedEntrance(
    visible: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.98f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "entranceScale",
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = LumetrixMotion.fadeSpec()),
        exit = fadeOut(animationSpec = LumetrixMotion.fadeSpec(220)),
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
    ) {
        content()
    }
}
