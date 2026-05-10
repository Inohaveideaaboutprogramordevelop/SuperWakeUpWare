package com.superwakeupware.microgames.games

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
 * PLATFORM BALANCE
 * Use the gyroscope to keep the character centred on a moving platform.
 * Stay within the safe zone for [HOLD_MS] cumulative milliseconds to win.
 */
class PlatformBalance @Inject constructor() : Microgame {
    override val id          = MicrogameId.PLATFORM_BALANCE
    override val instruction = "BALANCE!"
    override val durationMs  = 5_000L

    companion object {
        private const val HOLD_MS      = 2_500L  // must balance for 2.5 s total
        private const val SAFE_ZONE_PX = 80f     // half-width of safe zone
    }

    @androidx.compose.runtime.Composable
    override fun Content(onResult: (Boolean) -> Unit) {
        val context = LocalContext.current
        val sensorManager = remember {
            context.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
        }
        var tilt        by remember { mutableFloatStateOf(0f) }
        var balanceMs   by remember { mutableLongStateOf(0L) }
        var containerW  by remember { mutableIntStateOf(600) }
        var done        by remember { mutableStateOf(false) }

        DisposableEffect(Unit) {
            val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    // event.values[1] = rotation around Y axis (tilt left/right)
                    tilt = (tilt + event.values[1] * 8f).coerceIn(-300f, 300f)
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            if (gyro != null) {
                sensorManager.registerListener(listener, gyro, SensorManager.SENSOR_DELAY_GAME)
            }
            onDispose { sensorManager.unregisterListener(listener) }
        }

        // Accumulate balance time
        LaunchedEffect(Unit) {
            val tickMs = 50L
            val endMs  = System.currentTimeMillis() + durationMs
            while (!done && System.currentTimeMillis() < endMs) {
                delay(tickMs)
                if (abs(tilt) < SAFE_ZONE_PX) {
                    balanceMs += tickMs
                    if (balanceMs >= HOLD_MS) { done = true; onResult(true) }
                }
            }
            if (!done) { done = true; onResult(false) }
        }

        val charOffset = tilt.roundToInt()
        val platformX  = (containerW / 2 - 64).coerceAtLeast(0)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { containerW = it.size.width },
            contentAlignment = Alignment.BottomCenter,
        ) {
            // Platform
            Image(
                painter            = painterResource(R.drawable.sprite_platform),
                contentDescription = "Platform",
                modifier           = Modifier
                    .width(128.dp)
                    .height(24.dp)
                    .offset { IntOffset(0, -40) },
            )
            // Character tilts with gyro
            Image(
                painter            = painterResource(R.drawable.sprite_toad),
                contentDescription = "Toad",
                modifier           = Modifier
                    .size(56.dp)
                    .offset { IntOffset(charOffset, -88) },
            )
        }
    }

    private fun Modifier.onGloballyPositioned(action: (androidx.compose.ui.layout.LayoutCoordinates) -> Unit) =
        this.then(androidx.compose.ui.Modifier.onGloballyPositioned(action))
}
