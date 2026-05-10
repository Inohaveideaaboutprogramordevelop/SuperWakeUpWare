package com.superwakeupware.microgames.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.superwakeupware.R
import com.superwakeupware.microgames.Microgame
import com.superwakeupware.microgames.MicrogameId
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * ENTER THE PIPE
 * Mario stands still; a warp pipe appears at a random X. Swipe Mario
 * horizontally towards the pipe to enter it.
 */
class EnterPipe @Inject constructor() : Microgame {
    override val id          = MicrogameId.ENTER_PIPE
    override val instruction = "INTO THE PIPE!"
    override val durationMs  = 4_000L

    @androidx.compose.runtime.Composable
    override fun Content(onResult: (Boolean) -> Unit) {
        var containerWidth by remember { mutableIntStateOf(600) }
        val pipeX  = remember { (100..500).random().toFloat() }
        var marioX by remember { mutableFloatStateOf(containerWidth / 2f) }
        var entered by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(durationMs)
            if (!entered) onResult(false)
        }

        // Check proximity every frame
        LaunchedEffect(marioX) {
            if (!entered && abs(marioX - pipeX) < 48f) {
                entered = true
                onResult(true)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { containerWidth = it.size.width }
                .pointerInput(entered) {
                    if (entered) return@pointerInput
                    detectDragGestures { _, dragAmount ->
                        marioX = (marioX + dragAmount.x).coerceIn(0f, containerWidth.toFloat())
                    }
                },
        ) {
            // Pipe at fixed position
            Image(
                painter            = painterResource(R.drawable.sprite_pipe),
                contentDescription = "Warp Pipe",
                modifier           = Modifier
                    .size(72.dp, 96.dp)
                    .offset { IntOffset(pipeX.roundToInt() - 36, size.height - 96) },
            )
            // Mario draggable
            Image(
                painter            = painterResource(R.drawable.sprite_mario_walk),
                contentDescription = "Mario",
                modifier           = Modifier
                    .size(64.dp)
                    .offset { IntOffset(marioX.roundToInt() - 32, size.height - 80) },
            )
        }
    }

    // Extension for onGloballyPositioned captured width
    private fun Modifier.onGloballyPositioned(action: (androidx.compose.ui.layout.LayoutCoordinates) -> Unit) =
        this.then(androidx.compose.ui.Modifier.onGloballyPositioned(action))
}
