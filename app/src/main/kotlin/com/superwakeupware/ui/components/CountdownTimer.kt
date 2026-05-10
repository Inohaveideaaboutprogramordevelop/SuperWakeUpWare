package com.superwakeupware.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.superwakeupware.ui.theme.CoinGold
import com.superwakeupware.ui.theme.DangerRed
import com.superwakeupware.ui.theme.StarYellow
import com.superwakeupware.ui.theme.SuperWakeTypography
import kotlinx.coroutines.delay

/**
 * Pixel-font countdown that pulses red when ≤ 1 second remains.
 * [durationMs] is reset each time a new microgame starts.
 */
@Composable
fun CountdownTimer(durationMs: Long, onTimeUp: () -> Unit, modifier: Modifier = Modifier) {
    var remainingMs by remember(durationMs) { mutableLongStateOf(durationMs) }
    val pulse       = remember { Animatable(1f) }

    LaunchedEffect(durationMs) {
        val tickMs = 50L
        while (remainingMs > 0) {
            delay(tickMs)
            remainingMs -= tickMs
        }
        onTimeUp()
    }

    LaunchedEffect(remainingMs) {
        if (remainingMs <= 1_000L) {
            pulse.animateTo(1.3f, tween(120, easing = FastOutSlowInEasing))
            pulse.animateTo(1f,   tween(120))
        }
    }

    val color  = when {
        remainingMs <= 1_000L -> DangerRed
        remainingMs <= 2_000L -> com.superwakeupware.ui.theme.WarningOrange
        else                  -> CoinGold
    }
    val seconds = "%.1f".format(remainingMs / 1_000.0)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text     = seconds,
            style    = SuperWakeTypography.displayLarge,
            color    = color,
            modifier = Modifier.graphicsLayer { scaleX = pulse.value; scaleY = pulse.value },
        )
    }
}
