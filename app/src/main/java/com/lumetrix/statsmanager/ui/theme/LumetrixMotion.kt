package com.lumetrix.statsmanager.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

object LumetrixMotion {
    val EaseOutPremium = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    val EaseInOutPremium = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    const val TabSlideDuration = 420
    const val TabFadeDuration = 320
    const val TabScaleDuration = 420

    fun tabSlideSpec() = tween<IntOffset>(
        durationMillis = TabSlideDuration,
        easing = EaseOutPremium,
    )

    fun tabSlideOutSpec() = tween<IntOffset>(
        durationMillis = 360,
        easing = EaseOutPremium,
    )

    fun fadeSpec(durationMillis: Int = TabFadeDuration) = tween<Float>(
        durationMillis = durationMillis,
        easing = EaseInOutPremium,
    )

    fun scaleSpec(durationMillis: Int = TabScaleDuration) = tween<Float>(
        durationMillis = durationMillis,
        easing = EaseOutPremium,
    )
}
