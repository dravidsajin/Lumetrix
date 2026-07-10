package com.lumetrix.statsmanager.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LumetrixColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = TextPrimary,
    secondary = AccentSecondary,
    onSecondary = BackgroundPrimary,
    background = BackgroundPrimary,
    onBackground = TextPrimary,
    surface = BackgroundSecondary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = Divider,
    error = Danger,
)

@Composable
fun LumetrixTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundPrimary.toArgb()
            window.navigationBarColor = BackgroundPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = LumetrixColorScheme,
        typography = LumetrixTypography,
        content = content,
    )
}
