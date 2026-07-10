package com.lumetrix.statsmanager.ui.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lumetrix.statsmanager.core.time.DateUtils
import com.lumetrix.statsmanager.ui.theme.AccentGradientEnd
import com.lumetrix.statsmanager.ui.theme.AccentGradientStart
import com.lumetrix.statsmanager.ui.theme.AccentPrimary
import com.lumetrix.statsmanager.ui.theme.AccentSecondary
import com.lumetrix.statsmanager.ui.theme.GlassCard
import com.lumetrix.statsmanager.ui.theme.GlassCardBorder
import com.lumetrix.statsmanager.ui.theme.TextPrimary
import com.lumetrix.statsmanager.ui.theme.TextSecondary
import com.lumetrix.statsmanager.ui.theme.Warning
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Feature 1: Historical Day Browser
 *
 * A compact date selector showing the currently viewed date with left/right
 * navigation arrows and an optional "Today" jump button. Slides text as the
 * date changes to give a strong directional feel.
 */
@Composable
fun DateSelectorHeader(
    selectedDate: LocalDate,
    isViewingPastDay: Boolean,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = DateUtils.today()
    val canGoForward = selectedDate.isBefore(today)
    val canGoBack = selectedDate.isAfter(today.minusDays(30))

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ← Previous day
            NavArrow(
                icon = Icons.Rounded.ChevronLeft,
                enabled = canGoBack,
                onClick = onPreviousDay,
            )

            // Date pill
            AnimatedContent(
                targetState = selectedDate,
                transitionSpec = {
                    val dir = if (targetState.isAfter(initialState)) 1 else -1
                    slideInHorizontally { it * dir } togetherWith slideOutHorizontally { -it * dir }
                },
                label = "dateSlide",
            ) { date ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isViewingPastDay) GlassCard else Color.Transparent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isViewingPastDay) Brush.horizontalGradient(
                                listOf(AccentPrimary.copy(alpha = 0.15f), AccentSecondary.copy(alpha = 0.1f))
                            ) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                ) {
                    Text(
                        text = formatDateLabel(date, today),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isViewingPastDay) AccentPrimary else TextPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    )
                }
            }

            // → Next day
            NavArrow(
                icon = Icons.Rounded.ChevronRight,
                enabled = canGoForward,
                onClick = onNextDay,
            )
        }

        // "Jump to Today" pill — only visible when viewing a past day
        if (isViewingPastDay) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onTodayClick)
                    .background(Warning.copy(alpha = 0.12f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Today,
                    contentDescription = "Today",
                    tint = Warning,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Viewing past day — tap to return to Today",
                    style = MaterialTheme.typography.labelMedium,
                    color = Warning,
                )
            }
        }
    }
}

@Composable
private fun NavArrow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(GlassCard)
            .alpha(if (enabled) 1f else 0.3f)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) TextPrimary else TextSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

private fun formatDateLabel(date: LocalDate, today: LocalDate): String = when {
    date == today -> "Today"
    date == today.minusDays(1) -> "Yesterday"
    date == today.minusDays(2) -> "2 days ago"
    else -> date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
}
