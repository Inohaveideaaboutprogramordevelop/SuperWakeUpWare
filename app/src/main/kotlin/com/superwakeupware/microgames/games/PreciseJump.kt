package com.superwakeupware.microgames.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.superwakeupware.R
import com.superwakeupware.microgames.Microgame
import com.superwakeupware.microgames.MicrogameId
import com.superwakeupware.ui.theme.CoinGold
import com.superwakeupware.ui.theme.SuperWakeTypography
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * PRECISE JUMP!
 * A fire bar sweeps across the screen. A narrow "safe window" is marked with a coin.
 * Tap the screen at the exact moment Mario aligns with the window to jump over it.
 * Win: tap when fire bar is inside the safe zone. Lose: tap outside zone or time out.
 */
class PreciseJump @Inject constructor() : Microgame {
    override val id          = MicrogameId.PRECISE_JUMP
    override val instruction = "JUMP NOW!"
    override val durationMs  = 5_000L

    // The "safe" zone is centred at X = 0.55 (normalized) with half-width of 0.08
    private val SAFE_CENTER = 0.55f
    private val SAFE_HALF   = 0.08f

    @androidx.compose.runtime.Composable
    override fun Content(onResult: (Boolean) -> Unit) {
        var containerW by remember { mutableIntStateOf(600) }
        var containerH by remember { mutableIntStateOf(400) }
        var elapsed    by remember { mutableLongStateOf(0L) }
        var jumped     by remember { mutableStateOf(false) }
        var jumpY      by remember { mutableFloatStateOf(0f) }
        var done       by remember { mutableStateOf(false) }

        // Clock
        LaunchedEffect(Unit) {
            val start = System.currentTimeMillis()
            while (!done) {
                elapsed = System.currentTimeMillis() - start
                if (elapsed >= durationMs) { done = true; onResult(false) }
                delay(16L)
            }
        }

        // Jump arc animation
        val jumpAnim by animateFloatAsState(
            targetValue   = if (jumped) -120f else 0f,
            animationSpec = keyframes {
                durationMillis = 600
                -120f at 200 using FastOutSlowInEasing
                0f    at 600 using LinearEasing
            },
            finishedListener = { jumpY = 0f },
            label = "jump_arc",
        )

        // Normalised fire-bar position oscillates 0..1
        val fireBarFrac = ((elapsed % 2_400L) / 2_400f)
        val fireBarX    = (fireBarFrac * containerW).roundToInt()
        val inSafeZone  = abs(fireBarFrac - SAFE_CENTER) < SAFE_HALF

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { containerW = it.size.width; containerH = it.size.height }
                .pointerInput(done) {
                    if (!done) detectTapGestures {
                        if (jumped || done) return@detectTapGestures
                        jumped = true
                        if (inSafeZone) {
                            // Give jump time to animate, then win
                            kotlinx.coroutines.GlobalScope.kotlinx.coroutines.launch {
                                delay(350)
                                done = true
                                onResult(true)
                            }
                        } else {
                            done = true
                            onResult(false)
                        }
                    }
                },
        ) {
            val groundY = containerH - 80

            // Ground line (painted via Canvas in the real impl; placeholder here)

            // Safe zone marker (coin)
            Image(
                painter            = painterResource(R.drawable.sprite_coin),
                contentDescription = "Safe zone",
                modifier           = Modifier
                    .size(32.dp)
                    .offset { IntOffset((SAFE_CENTER * containerW - 16).roundToInt(), groundY - 48) },
            )

            // Fire bar
            Image(
                painter            = painterResource(R.drawable.sprite_fire_bar),
                contentDescription = "Fire Bar",
                modifier           = Modifier
                    .size(24.dp, 80.dp)
                    .offset { IntOffset(fireBarX - 12, groundY - 80) },
            )

            // Mario
            Image(
                painter            = painterResource(R.drawable.sprite_mario_walk),
                contentDescription = "Mario",
                modifier           = Modifier
                    .size(56.dp)
                    .offset { IntOffset(containerW / 4, (groundY - 64 + jumpAnim).roundToInt()) },
            )

            // Timer hint
            Text(
                text      = "TAP TO JUMP",
                style     = SuperWakeTypography.labelSmall,
                color     = CoinGold,
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
            )
        }
    }

    private fun Modifier.onGloballyPositioned(action: (androidx.compose.ui.layout.LayoutCoordinates) -> Unit) =
        this.then(androidx.compose.ui.Modifier.onGloballyPositioned(action))
}
