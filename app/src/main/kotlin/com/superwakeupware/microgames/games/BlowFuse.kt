package com.superwakeupware.microgames.games

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.superwakeupware.R
import com.superwakeupware.microgames.Microgame
import com.superwakeupware.microgames.MicrogameId
import com.superwakeupware.ui.theme.SuperWakeTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * BLOW THE FUSE
 * A Bob-omb sits with a burning fuse. Blow into the microphone (loud sustained
 * noise) to extinguish it before the fuse burns down.
 */
class BlowFuse @Inject constructor() : Microgame {
    override val id          = MicrogameId.BLOW_FUSE
    override val instruction = "BLOW IT OUT!"
    override val durationMs  = 5_000L

    private val SAMPLE_RATE      = 44_100
    private val RMS_THRESHOLD    = 1_500.0   // ~medium breath amplitude
    private val SUSTAINED_FRAMES = 12        // ~12 × 50 ms = 600 ms of sustained blowing

    @OptIn(ExperimentalPermissionsApi::class)
    @androidx.compose.runtime.Composable
    override fun Content(onResult: (Boolean) -> Unit) {
        val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
        var fuseProgress  by remember { mutableFloatStateOf(1f) }  // 1 = full fuse, 0 = exploded
        var blowFrames    by remember { mutableIntStateOf(0) }
        var done          by remember { mutableStateOf(false) }

        // Animate fuse shrinking to zero over the game duration
        val fuseAnim by animateFloatAsState(
            targetValue   = if (done) fuseProgress else 0f,
            animationSpec = tween(durationMs.toInt(), easing = LinearEasing),
            label         = "fuse",
        )

        LaunchedEffect(Unit) {
            delay(durationMs)
            if (!done) onResult(false)
        }

        // Microphone RMS reader
        LaunchedEffect(micPermission.status.isGranted) {
            if (!micPermission.status.isGranted) {
                micPermission.launchPermissionRequest()
                return@LaunchedEffect
            }
            val bufSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufSize,
            )
            recorder.startRecording()
            withContext(Dispatchers.IO) {
                val buf = ShortArray(bufSize / 2)
                while (isActive && !done) {
                    val read = recorder.read(buf, 0, buf.size)
                    if (read > 0) {
                        val rms = sqrt(buf.take(read).sumOf { it.toLong() * it }.toDouble() / read)
                        if (rms > RMS_THRESHOLD) blowFrames++ else blowFrames = 0
                        if (blowFrames >= SUSTAINED_FRAMES) {
                            done = true
                            withContext(Dispatchers.Main) { onResult(true) }
                        }
                    }
                    delay(50)
                }
                recorder.stop()
                recorder.release()
            }
        }

        Column(
            modifier            = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter            = painterResource(R.drawable.sprite_bobomb),
                contentDescription = "Bob-omb",
                modifier           = Modifier.size(128.dp),
            )
            Spacer(Modifier.height(12.dp))
            // Fuse bar
            Box(
                modifier        = Modifier
                    .width(160.dp)
                    .height(12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(androidx.compose.ui.graphics.Color(0xFFFF7900), size = size.copy(width = size.width * fuseAnim))
                }
            }
            Spacer(Modifier.height(16.dp))
            if (!micPermission.status.isGranted) {
                Text("Microphone needed!", style = SuperWakeTypography.bodyLarge, textAlign = TextAlign.Center)
            }
        }
    }
}
