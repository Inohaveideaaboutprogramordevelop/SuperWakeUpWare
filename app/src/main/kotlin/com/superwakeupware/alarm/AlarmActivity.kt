package com.superwakeupware.alarm

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.superwakeupware.ui.screens.AlarmScreen
import com.superwakeupware.ui.theme.SuperWakeUpTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Full-screen activity shown on the lock screen.
 * The heavy logic lives in [AlarmViewModel]; this class is intentionally thin.
 */
@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    private val viewModel: AlarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lock screen, keep screen on
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val alarmId    = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        val alarmLabel = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "Wake Up!"
        viewModel.startSession(alarmId, alarmLabel)

        setContent {
            SuperWakeUpTheme {
                val gameState by viewModel.gameState.collectAsState()
                AlarmScreen(
                    gameState  = gameState,
                    alarmLabel = alarmLabel,
                    onResult   = { won -> viewModel.reportResult(won) },
                    onContinue = {
                        viewModel.continueAfterResult()
                        if (viewModel.isDismissed()) finish()
                    },
                )
            }
        }
    }

    override fun onBackPressed() {
        // Block back-navigation — user MUST complete a microgame to leave
    }
}
