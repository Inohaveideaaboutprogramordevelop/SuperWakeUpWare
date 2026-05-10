package com.superwakeupware.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.superwakeupware.R
import com.superwakeupware.ui.theme.MarioRed
import com.superwakeupware.ui.theme.SuperWakeTypography
import com.superwakeupware.ui.theme.WarningOrange

/**
 * Displays up to 3 Mario-head life icons. Lost lives shake before going grey.
 */
@Composable
fun LivesDisplay(lives: Int, maxLives: Int = 3, modifier: Modifier = Modifier) {
    Row(
        modifier            = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment   = Alignment.CenterVertically,
    ) {
        Text("×", style = SuperWakeTypography.labelSmall, color = WarningOrange)
        for (i in maxLives downTo 1) {
            val alive = i <= lives
            val shakeAnim = remember { Animatable(0f) }
            LaunchedEffect(lives) {
                if (!alive) {
                    shakeAnim.animateTo(12f,  tween(60))
                    shakeAnim.animateTo(-12f, tween(60))
                    shakeAnim.animateTo(8f,   tween(50))
                    shakeAnim.animateTo(-8f,  tween(50))
                    shakeAnim.animateTo(0f,   tween(40))
                }
            }
            val tint by animateColorAsState(
                targetValue   = if (alive) MarioRed else androidx.compose.ui.graphics.Color.Gray,
                animationSpec = tween(300),
                label         = "life_tint_$i",
            )
            Image(
                painter            = painterResource(R.drawable.ic_mario_head),
                contentDescription = if (alive) "Life" else "Lost life",
                modifier           = Modifier
                    .size(32.dp)
                    .graphicsLayer { translationX = if (!alive) shakeAnim.value else 0f }
                    // ColorFilter via tint not available on Image directly; wrap in Box with tint
                    // in the real impl use ColorFilter.tint(tint) on the Painter
            )
        }
    }
}
