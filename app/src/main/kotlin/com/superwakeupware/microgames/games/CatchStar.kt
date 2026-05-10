package com.superwakeupware.microgames.games

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.superwakeupware.R
import com.superwakeupware.microgames.Microgame
import com.superwakeupware.microgames.MicrogameId
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * CATCH THE SUPER STAR
 * A star bounces around the screen (billiard physics). Tap it to win.
 */
class CatchStar @Inject constructor() : Microgame {
    override val id          = MicrogameId.CATCH_STAR
    override val instruction = "GRAB THE STAR!"
    override val durationMs  = 5_000L

    private val STAR_SIZE_PX = 80f

    @androidx.compose.runtime.Composable
    override fun Content(onResult: (Boolean) -> Unit) {
        var size  by remember { mutableStateOf(IntSize.Zero) }
        var pos   by remember { mutableStateOf(Offset(200f, 300f)) }
        var vel   by remember { mutableStateOf(Offset(Random.nextFloat() * 6 + 4, Random.nextFloat() * 6 + 4)) }
        var done  by remember { mutableStateOf(false) }

        // Physics tick at ~60 fps
        LaunchedEffect(size) {
            if (size == IntSize.Zero) return@LaunchedEffect
            val end = System.currentTimeMillis() + durationMs
            while (!done && System.currentTimeMillis() < end) {
                delay(16L)
                var nx = pos.x + vel.x
                var ny = pos.y + vel.y
                var vx = vel.x
                var vy = vel.y
                if (nx <= 0 || nx + STAR_SIZE_PX >= size.width)  { vx = -vx; nx = nx.coerceIn(0f, size.width - STAR_SIZE_PX) }
                if (ny <= 0 || ny + STAR_SIZE_PX >= size.height) { vy = -vy; ny = ny.coerceIn(0f, size.height - STAR_SIZE_PX) }
                pos = Offset(nx, ny)
                vel = Offset(vx, vy)
            }
            if (!done) onResult(false)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { size = it.size }
                .pointerInput(done) {
                    if (!done) detectTapGestures { tap ->
                        val starRect = Rect(pos.x, pos.y, pos.x + STAR_SIZE_PX, pos.y + STAR_SIZE_PX)
                        if (starRect.contains(tap)) { done = true; onResult(true) }
                    }
                },
        ) {
            Image(
                painter            = painterResource(R.drawable.sprite_super_star),
                contentDescription = "Super Star",
                modifier           = Modifier
                    .size(80.dp)
                    .offset { IntOffset(pos.x.roundToInt(), pos.y.roundToInt()) },
            )
        }
    }
}
