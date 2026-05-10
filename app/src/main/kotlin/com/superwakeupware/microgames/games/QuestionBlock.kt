package com.superwakeupware.microgames.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superwakeupware.R
import com.superwakeupware.microgames.Microgame
import com.superwakeupware.microgames.MicrogameId
import com.superwakeupware.ui.theme.CoinGold
import com.superwakeupware.ui.theme.SuperWakeTypography
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * QUESTION MARK BLOCK
 * Tap the block rapidly. After [TAPS_REQUIRED] taps a coin pops out and the game is won.
 */
class QuestionBlock @Inject constructor() : Microgame {
    override val id          = MicrogameId.QUESTION_BLOCK
    override val instruction = "HIT IT!"
    override val durationMs  = 5_000L

    companion object { private const val TAPS_REQUIRED = 5 }

    @androidx.compose.runtime.Composable
    override fun Content(onResult: (Boolean) -> Unit) {
        var tapCount  by remember { mutableIntStateOf(0) }
        var coinPop   by remember { mutableStateOf(false) }
        var done      by remember { mutableStateOf(false) }

        // Bump animation on each tap
        val bumpAnim  = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            delay(durationMs)
            if (!done) onResult(false)
        }

        LaunchedEffect(tapCount) {
            if (tapCount > 0) bumpAnim.animateTo(-20f, tween(60)).also { bumpAnim.animateTo(0f, tween(80)) }
            if (tapCount >= TAPS_REQUIRED && !done) {
                done    = true
                coinPop = true
                delay(400)
                onResult(true)
            }
        }

        Column(
            modifier            = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Coin pop-up
            if (coinPop) {
                Image(
                    painter            = painterResource(R.drawable.sprite_coin),
                    contentDescription = "Coin",
                    modifier           = Modifier.size(48.dp),
                )
            } else {
                Spacer(Modifier.height(48.dp))
            }

            Spacer(Modifier.height(8.dp))

            // The block itself
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer { translationY = bumpAnim.value }
                    .pointerInput(done) {
                        if (!done) detectTapGestures { tapCount++ }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter            = painterResource(R.drawable.sprite_question_block),
                    contentDescription = "? Block",
                    modifier           = Modifier.fillMaxSize(),
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text      = "${tapCount}/${TAPS_REQUIRED}",
                style     = SuperWakeTypography.labelSmall,
                color     = CoinGold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
