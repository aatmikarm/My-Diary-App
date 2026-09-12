package com.aatmik.mydiary.streak

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun StreakBadge(
    streakCount: Int,
    modifier: Modifier = Modifier,
    isAtRisk: Boolean = false,
    activeColor: Color = Color(0xFFFF7A1A),
    onClick: (() -> Unit)? = null
) {
    if (streakCount <= 0) return

    var expanded by remember { mutableStateOf(false) }

    val tint = if (isAtRisk) {
        activeColor.copy(alpha = 0.5f)
    } else {
        activeColor
    }

    /*
     * ---------------------------------------------------------
     * FLAME ANIMATION
     * ---------------------------------------------------------
     *
     * This continuously:
     * 0.92x -> 1.18x -> 0.92x -> 1.18x ...
     *
     * IMPORTANT:
     * We do NOT disable the animation when isAtRisk is true.
     * isAtRisk only changes the visual appearance.
     */
    val infiniteTransition = rememberInfiniteTransition(
        label = "streak_flame_transition"
    )

    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 650,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "streak_flame_scale"
    )

    val flameAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 650,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "streak_flame_alpha"
    )

    /*
     * Auto collapse after opening.
     */
    LaunchedEffect(expanded) {
        if (expanded) {
            delay(2200)
            expanded = false
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(
                tint.copy(alpha = 0.12f)
            )
            .clickable {
                expanded = !expanded
                onClick?.invoke()
            }
            .padding(
                horizontal = 10.dp,
                vertical = 4.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        /*
         * Flame
         *
         * scale() is applied directly to the Icon so the
         * animation is impossible to miss.
         */
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = "Streak: $streakCount days",
            tint = tint.copy(alpha = flameAlpha),
            modifier = Modifier
                .size(20.dp)
                .scale(flameScale)
        )

        Spacer(
            modifier = Modifier.width(5.dp)
        )

        Text(
            text = streakCount.toString(),
            color = tint,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        AnimatedVisibility(
            visible = expanded,
            enter = expandHorizontally(
                animationSpec = tween(250)
            ) + fadeIn(
                animationSpec = tween(250)
            ),
            exit = shrinkHorizontally(
                animationSpec = tween(200)
            ) + fadeOut(
                animationSpec = tween(150)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                Text(
                    text = if (streakCount == 1) {
                        "Day Streak"
                    } else {
                        "Days Streak"
                    },
                    color = tint,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}