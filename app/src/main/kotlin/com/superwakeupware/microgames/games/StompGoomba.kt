package com.superwakeupware.microgames.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.superwakeupware.R
import com.superwakeupware.microgames.Microgame
import com.superwakeupware.microgames.MicrogameId
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * STOMP THE GOOMBA
 * A Goomba bounces horizontally. Tap it before the timer expires.
 */
class StompGoomba @Inject constructor() : Microgame {
    override val id          = MicrogameId.STOMP_GOOMBA
    override val instruction = "STOMP IT!"
    override val durationMs  = 4_500L

    @androidx.compose.runtime.Composable
    override fun Content(onResult: (Boolean) -> Unit) {
        var containerWidth by remember { mutableIntStateOf(1) }
        var stomped        by remember { mutableStateOf(false) }

        // Animate X position across the full container width
        val xAnim = rememberInfiniteTransition(label = "goomba_x")
        val rawX  by xAnim.animateFloat(
            initialValue  = 0f,
            targetValue   = 1f,
            animationSpec = infiniteRepeatable(
                animation  = tween(durationMillis = 1_200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "x_frac",
        )

        // Auto-fail on timeout
        LaunchedEffect(Unit) {
            delay(durationMs)
            if (!stomped) onResult(false)
        }

        val goombaSizePx = 96.dp
        val xPx = (rawX * (containerWidth - goombaSizePx.value)).roundToInt()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { containerWidth = it.size.width }
                .pointerInput(stomped) {
                    if (stomped) return@pointerInput
                    detectTapGestures { tap: Offset ->
                        val goombaRect = androidx.compose.ui.geometry.Rect(
                            left   = xPx.toFloat(),
                            top    = size.height / 2f - 48f,
                            right  = xPx + goombaSizePx.value,
                            bottom = size.height / 2f + 48f,
                        )
                        if (goombaRect.contains(tap)) {
                            stomped = true
                            onResult(true)
                        }
                    }
                },
        ) {
            if (!stomped) {
                Image(
                    painter            = painterResource(R.drawable.sprite_goomba),
                    contentDescription = "Goomba",
                    modifier           = Modifier
                        .size(96.dp)
                        .offset { IntOffset(xPx, (size.height / 2 - 48)) },
                )
            }
        }
    }
}
