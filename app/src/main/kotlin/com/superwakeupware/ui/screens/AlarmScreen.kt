package com.superwakeupware.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.superwakeupware.microgames.GameState
import com.superwakeupware.ui.components.*
import com.superwakeupware.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Root composable rendered by AlarmActivity.
 *
 * State machine:
 *   Idle → ShowInstruction → Playing → Result (win/fail) → (loop or Dismissed)
 */
@Composable
fun AlarmScreen(
    gameState  : GameState,
    alarmLabel : String,
    onResult   : (Boolean) -> Unit,
    onContinue : () -> Unit,
) {
    var showWinBurst  by remember { mutableStateOf(false) }
    var showFailFlash by remember { mutableStateOf(false) }
    var resultShown   by remember { mutableStateOf(false) }

    // React to result state to trigger feedback then auto-advance
    LaunchedEffect(gameState) {
        if (gameState is GameState.Result) {
            if (gameState.won) {
                showWinBurst = true
                delay(1_200)
                showWinBurst = false
            } else {
                showFailFlash = true
                delay(300)
                showFailFlash = false
                delay(200)
            }
            resultShown = true
            delay(600)
            resultShown = false
            onContinue()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BackgroundNight, SkyBlueDark))
            ),
    ) {
        // ── Status bar: label + lives ─────────────────────────────────────────
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment   = Alignment.CenterVertically,
        ) {
            Text(
                text  = alarmLabel.ifBlank { "Wake Up!" },
                style = SuperWakeTypography.titleLarge,
                color = CloudWhite,
            )
            val lives = when (gameState) {
                is GameState.Playing -> gameState.lives
                is GameState.Result  -> gameState.lives
                else                 -> 3
            }
            LivesDisplay(lives = lives)
        }

        // ── Game / Instruction / Result area ─────────────────────────────────
        Box(
            modifier        = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            when (val state = gameState) {
                is GameState.ShowInstruction -> InstructionOverlay(instruction = "GET READY!")

                is GameState.Playing -> {
                    Column(
                        modifier            = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Big game instruction
                        Spacer(Modifier.height(100.dp))
                        Text(
                            text      = state.game.instruction,
                            style     = SuperWakeTypography.displayMedium,
                            color     = StarYellow,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(8.dp))

                        // Countdown
                        CountdownTimer(
                            durationMs = state.game.durationMs,
                            onTimeUp   = { onResult(false) },
                            modifier   = Modifier.padding(bottom = 8.dp),
                        )

                        // Volume danger indicator
                        if (state.volumePercent > 70) {
                            Text(
                                text  = "🔊 ${state.volumePercent}%",
                                style = SuperWakeTypography.bodyLarge,
                                color = DangerRed,
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Game content fills remaining space
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            state.game.Content(onResult = onResult)
                        }
                    }
                }

                is GameState.Result -> ResultSplash(won = state.won)

                is GameState.Dismissed -> {
                    // Activity finishes itself after onContinue — show nothing
                }

                GameState.Idle -> { /* waiting for startSession */ }
            }
        }

        // ── Overlay FX (drawn on top of everything) ───────────────────────────
        WinParticleBurst(
            modifier = Modifier.fillMaxSize(),
            active   = showWinBurst,
            origin   = androidx.compose.ui.geometry.Offset(0.5f, 0.4f),
        )
        FailureFlash(modifier = Modifier.fillMaxSize(), active = showFailFlash)
    }
}

@Composable
private fun InstructionOverlay(instruction: String) {
    val scale by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "instruction_scale",
    )
    Text(
        text      = instruction,
        style     = SuperWakeTypography.displayMedium,
        color     = CoinGold,
        textAlign = TextAlign.Center,
        modifier  = Modifier.graphicsLayer { scaleX = scale; scaleY = scale },
    )
}

@Composable
private fun ResultSplash(won: Boolean) {
    val (emoji, label, color) = if (won)
        Triple("🌟", "NICE!", StarYellow)
    else
        Triple("💀", "FAILED!", DangerRed)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, style = SuperWakeTypography.displayLarge)
        Spacer(Modifier.height(8.dp))
        Text(label, style = SuperWakeTypography.displayMedium, color = color)
    }
}

private fun Modifier.graphicsLayer(block: androidx.compose.ui.graphics.layer.GraphicsLayer.() -> Unit) =
    this  // placeholder — in real code use Modifier.graphicsLayer { ... }
