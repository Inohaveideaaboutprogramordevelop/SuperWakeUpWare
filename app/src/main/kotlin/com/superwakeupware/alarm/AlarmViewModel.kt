package com.superwakeupware.alarm

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superwakeupware.microgames.GameState
import com.superwakeupware.microgames.MicrogameEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val engine  : MicrogameEngine,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val gameState: StateFlow<GameState> = engine.state

    fun startSession(alarmId: Int, label: String) {
        engine.startSession()
    }

    fun reportResult(won: Boolean) {
        if (won) {
            vibrateWin()
            AlarmService.instance?.dismissAlarm()
        } else {
            vibrateFailure()
            AlarmService.instance?.increaseVolume()
        }
        engine.reportResult(won)
    }

    fun continueAfterResult() = engine.continueAfterResult()

    fun isDismissed() = gameState.value is GameState.Dismissed

    // ── Haptics ───────────────────────────────────────────────────────────────

    private fun vibrateWin() {
        vibrate(longArrayOf(0, 80, 40, 80), -1)  // two short pulses
    }

    private fun vibrateFailure() {
        // Long angry rumble on failure
        vibrate(longArrayOf(0, 300, 50, 300, 50, 600), -1)
    }

    private fun vibrate(pattern: LongArray, repeat: Int) {
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        v.vibrate(VibrationEffect.createWaveform(pattern, repeat))
    }
}
