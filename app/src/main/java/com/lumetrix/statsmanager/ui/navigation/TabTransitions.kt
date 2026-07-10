package com.lumetrix.statsmanager.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import com.lumetrix.statsmanager.ui.theme.LumetrixMotion

fun AnimatedContentTransitionScope<Int>.lumetrixTabTransition(): ContentTransform {
    val forward = targetState > initialState
    val enterSlide = if (forward) { width: Int -> (width * 0.22f).toInt() } else { width: Int -> -(width * 0.22f).toInt() }
    val exitSlide = if (forward) { width: Int -> -(width * 0.14f).toInt() } else { width: Int -> (width * 0.14f).toInt() }

    return (
        slideInHorizontally(
            initialOffsetX = enterSlide,
            animationSpec = LumetrixMotion.tabSlideSpec(),
        ) + fadeIn(animationSpec = LumetrixMotion.fadeSpec()) + scaleIn(
            initialScale = 0.965f,
            animationSpec = LumetrixMotion.scaleSpec(),
        )
        ) togetherWith (
        slideOutHorizontally(
            targetOffsetX = exitSlide,
            animationSpec = LumetrixMotion.tabSlideOutSpec(),
        ) + fadeOut(animationSpec = tween(durationMillis = 260, easing = LumetrixMotion.EaseInOutPremium)) + scaleOut(
            targetScale = 0.965f,
            animationSpec = tween(durationMillis = 360, easing = LumetrixMotion.EaseOutPremium),
        )
        )
}
