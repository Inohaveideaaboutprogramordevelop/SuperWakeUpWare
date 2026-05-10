package com.superwakeupware.microgames.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.superwakeupware.microgames.Microgame
import com.superwakeupware.microgames.MicrogameId
import com.superwakeupware.ui.theme.CoinGold
import com.superwakeupware.ui.theme.MarioRed
import com.superwakeupware.ui.theme.PipeGreen
import com.superwakeupware.ui.theme.SuperWakeTypography
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * TURN THE CRANK
 * Draw circular motions to accumulate rotation. Reach [TARGET_DEGREES] to open the gate.
 */
class TurnCrank @Inject constructor() : Microgame {
    override val id          = MicrogameId.TURN_CRANK
    override val instruction = "CRANK IT!"
    override val durationMs  = 5_000L

    private val TARGET_DEGREES = 720f  // two full rotations

    @androidx.compose.runtime.Composable
    override fun Content(onResult: (Boolean) -> Unit) {
        var totalDeg   by remember { mutableFloatStateOf(0f) }
        var lastAngle  by remember { mutableStateOf<Float?>(null) }
        var crankAngle by remember { mutableFloatStateOf(0f) }
        var done       by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(durationMs)
            if (!done) onResult(false)
        }

        val progress = (totalDeg / TARGET_DEGREES).coerceIn(0f, 1f)

        Column(
            modifier            = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text      = "TURN: ${totalDeg.toInt()}° / ${TARGET_DEGREES.toInt()}°",
                style     = SuperWakeTypography.labelSmall,
                color     = CoinGold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .size(220.dp)
                    .pointerInput(done) {
                        if (done) return@pointerInput
                        detectDragGestures(
                            onDragEnd   = { lastAngle = null },
                            onDragCancel = { lastAngle = null },
                        ) { change, _ ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val v = change.position - center
                            if (sqrt(v.x * v.x + v.y * v.y) < 20f) return@detectDragGestures
                            val angle = Math.toDegrees(atan2(v.y.toDouble(), v.x.toDouble())).toFloat()
                            val prev = lastAngle
                            if (prev != null) {
                                var delta = angle - prev
                                // Normalise to -180..180
                                if (delta > 180f) delta -= 360f
                                if (delta < -180f) delta += 360f
                                totalDeg  += delta
                                crankAngle = (crankAngle + delta) % 360f
                                if (totalDeg >= TARGET_DEGREES && !done) {
                                    done = true; onResult(true)
                                }
                            }
                            lastAngle = angle
                        }
                    },
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r  = size.minDimension / 2f - 12f

                // Progress arc
                drawArc(
                    color       = PipeGreen,
                    startAngle  = -90f,
                    sweepAngle  = 360f * progress,
                    useCenter   = false,
                    style       = Stroke(width = 16f, cap = StrokeCap.Round),
                )
                // Background arc
                drawArc(
                    color       = Color(0xFF2A2A4A),
                    startAngle  = -90f + 360f * progress,
                    sweepAngle  = 360f * (1f - progress),
                    useCenter   = false,
                    style       = Stroke(width = 16f, cap = StrokeCap.Round),
                )
                // Crank handle
                rotate(crankAngle, Offset(cx, cy)) {
                    drawLine(
                        color       = MarioRed,
                        start       = Offset(cx, cy),
                        end         = Offset(cx + r * 0.7f, cy),
                        strokeWidth = 10f,
                        cap         = StrokeCap.Round,
                    )
                    drawCircle(color = CoinGold, radius = 14f, center = Offset(cx + r * 0.7f, cy))
                }
                // Centre knob
                drawCircle(color = Color.White, radius = 10f, center = Offset(cx, cy))
            }
        }
    }
}
