package com.superwakeupware.microgames.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.superwakeupware.microgames.Microgame
import com.superwakeupware.microgames.MicrogameId
import com.superwakeupware.ui.theme.CoinGold
import com.superwakeupware.ui.theme.SkyBlue
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.math.sqrt

/**
 * FOLLOW THE PATH
 * A dotted line connects two coins. Drag your finger along it without going off-track.
 */
class FollowPath @Inject constructor() : Microgame {
    override val id          = MicrogameId.FOLLOW_PATH
    override val instruction = "TRACE IT!"
    override val durationMs  = 5_000L

    private val TOLERANCE_PX = 60f   // allowed deviation from path

    @androidx.compose.runtime.Composable
    override fun Content(onResult: (Boolean) -> Unit) {
        var canvasSize   by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
        var trailPoints  by remember { mutableStateOf(listOf<Offset>()) }
        var progress     by remember { mutableFloatStateOf(0f) }
        var done         by remember { mutableStateOf(false) }
        var failed       by remember { mutableStateOf(false) }

        // Path defined as normalised fractions (0..1), resolved after canvas is measured
        val pathFracs = listOf(
            Offset(0.15f, 0.8f), Offset(0.3f, 0.4f), Offset(0.5f, 0.6f),
            Offset(0.7f, 0.3f), Offset(0.85f, 0.6f),
        )

        LaunchedEffect(Unit) {
            delay(durationMs)
            if (!done) onResult(false)
        }

        fun pathPointAt(t: Float, size: androidx.compose.ui.unit.IntSize): Offset {
            val pts = pathFracs.map { Offset(it.x * size.width, it.y * size.height) }
            val idx = (t * (pts.size - 1)).toInt().coerceIn(0, pts.size - 2)
            val frac = (t * (pts.size - 1)) - idx
            return lerp(pts[idx], pts[idx + 1], frac)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(done) {
                    if (done) return@pointerInput
                    detectDragGestures(
                        onDragEnd = {
                            if (!done && !failed && progress >= 0.95f) {
                                done = true; onResult(true)
                            } else if (!done && !failed) {
                                done = true; onResult(false)
                            }
                        }
                    ) { change, _ ->
                        val pos = change.position
                        if (canvasSize == androidx.compose.ui.unit.IntSize.Zero) return@detectDragGestures
                        // Find the nearest path point and check deviation
                        val nearest = pathPointAt(progress, canvasSize)
                        val dist = dist(pos, nearest)
                        if (dist > TOLERANCE_PX && !failed) {
                            failed = true; done = true; onResult(false)
                            return@detectDragGestures
                        }
                        // Advance progress based on proximity to path end
                        progress = ((progress + 0.008f).coerceAtMost(1f))
                        trailPoints = trailPoints + pos
                    }
                }
                .onGloballyPositioned { canvasSize = it.size },
        ) {
            if (canvasSize == androidx.compose.ui.unit.IntSize.Zero) return@Canvas

            val pts = pathFracs.map { Offset(it.x * size.width, it.y * size.height) }

            // Draw dotted guide path
            val guidePath = Path().apply {
                moveTo(pts.first().x, pts.first().y)
                pts.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(guidePath, Color(0xFF4466AA), style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(20f, 14f))))

            // Start / end coins
            drawCircle(CoinGold, radius = 20f, center = pts.first())
            drawCircle(CoinGold, radius = 20f, center = pts.last())

            // User trail
            if (trailPoints.size > 1) {
                val trail = Path().apply {
                    moveTo(trailPoints.first().x, trailPoints.first().y)
                    trailPoints.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(trail, SkyBlue, style = Stroke(width = 10f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }

    private fun lerp(a: Offset, b: Offset, t: Float) = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
    private fun dist(a: Offset, b: Offset) = sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y))

    private fun Modifier.onGloballyPositioned(action: (androidx.compose.ui.layout.LayoutCoordinates) -> Unit) =
        this.then(androidx.compose.ui.Modifier.onGloballyPositioned(action))
}
