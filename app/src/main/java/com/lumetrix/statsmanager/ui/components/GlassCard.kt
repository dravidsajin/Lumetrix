package com.lumetrix.statsmanager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.GlassCard
import com.lumetrix.statsmanager.ui.theme.GlassCardBorder
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = LumetrixTokens.CardRadius,
    glowBorder: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val border = if (glowBorder) {
        BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(listOf(AccentPrimary.copy(alpha = 0.5f), AccentSecondary.copy(alpha = 0.35f))),
        )
    } else {
        BorderStroke(1.dp, GlassCardBorder)
    }

    Surface(
        modifier = modifier.clip(shape),
        shape = shape,
        color = GlassCard,
        border = border,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(LumetrixTokens.CardSpacing),
            content = content,
        )
    }
}

@Composable
fun GradientGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(LumetrixTokens.CardRadius)

    Surface(
        modifier = modifier.clip(shape),
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(
                    AccentPrimary.copy(alpha = 0.6f),
                    AccentSecondary.copy(alpha = 0.4f),
                    AccentPrimary.copy(alpha = 0.2f),
                ),
            ),
        ),
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .padding(LumetrixTokens.CardSpacing),
            content = content,
        )
    }
}
