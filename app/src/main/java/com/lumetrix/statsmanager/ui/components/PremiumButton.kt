package com.lumetrix.statsmanager.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lumetrix.statsmanager.ui.theme.AccentGradientEnd
import com.lumetrix.statsmanager.ui.theme.AccentGradientStart
import com.lumetrix.statsmanager.ui.theme.LumetrixTokens
import com.lumetrix.statsmanager.ui.theme.TextPrimary

@Composable
fun PremiumPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(stiffness = 400f),
        label = "buttonScale",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .height(LumetrixTokens.ButtonHeight)
            .clip(RoundedCornerShape(999.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(AccentGradientStart, AccentGradientEnd),
                ),
            )
            .clickable(
                enabled = enabled,
                interactionSource = MutableInteractionSource(),
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
        )
    }
}

@Composable
fun FocusModeCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "focusModeScale",
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(LumetrixTokens.CardRadius))
            .background(
                if (selected) {
                    Brush.linearGradient(
                        listOf(
                            AccentGradientStart.copy(alpha = 0.25f),
                            AccentGradientEnd.copy(alpha = 0.15f),
                        ),
                    )
                } else {
                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                },
            ),
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null,
                    onClick = onClick,
                ),
            glowBorder = selected,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
        }
    }
}

private object Spring {
    const val StiffnessMediumLow = 300f
}
