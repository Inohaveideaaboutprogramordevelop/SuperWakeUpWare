package com.superwakeupware.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.superwakeupware.ui.theme.CoinGold
import com.superwakeupware.ui.theme.MarioRed
import com.superwakeupware.ui.theme.StarYellow
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val x: Float, val y: Float,
    val vx: Float, val vy: Float,
    val color: Color,
    val radius: Float,
    var life: Float = 1f,  // 1 = fresh, 0 = dead
)

/**
 * Burst of coins/stars erupting from the given origin.
 * Call [active] = true to trigger a new burst; it auto-fades and stops.
 */
@Composable
fun WinParticleBurst(modifier: Modifier = Modifier, active: Boolean, origin: Offset = Offset(0.5f, 0.5f)) {
    val palette = listOf(CoinGold, StarYellow, MarioRed, Color(0xFF5C94FC), Color.White)
    var particles by remember { mutableStateOf(listOf<Particle>()) }

    LaunchedEffect(active) {
        if (!active) return@LaunchedEffect
        // Spawn 40 particles
        particles = (0 until 40).map {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
            val speed = Random.nextFloat() * 12f + 6f
            Particle(
                x      = origin.x,
                y      = origin.y,
                vx     = cos(angle) * speed,
                vy     = sin(angle) * speed,
                color  = palette.random(),
                radius = Random.nextFloat() * 8f + 4f,
            )
        }
        // Tick physics
        repeat(60) {
            delay(16L)
            particles = particles.map { p ->
                p.copy(
                    x    = p.x + p.vx,
                    y    = p.y + p.vy + 0.4f,  // gravity
                    life = p.life - 0.03f,
                )
            }.filter { it.life > 0f }
        }
        particles = emptyList()
    }

    Canvas(modifier = modifier) {
        particles.forEach { p ->
            drawCircle(
                color  = p.color.copy(alpha = p.life),
                radius = p.radius * p.life,
                center = Offset(p.x * size.width, p.y * size.height),
            )
        }
    }
}

/** Flashes the screen red with decreasing alpha — used on microgame failure. */
@Composable
fun FailureFlash(modifier: Modifier = Modifier, active: Boolean) {
    val alpha by animateFloatAsState(
        targetValue   = if (active) 0.55f else 0f,
        animationSpec = if (active) tween(50) else tween(400),
        label         = "fail_flash",
    )
    Canvas(modifier = modifier) {
        drawRect(MarioRed.copy(alpha = alpha), size = size)
    }
}
