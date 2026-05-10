package com.superwakeupware

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.superwakeupware.ui.screens.SetAlarmScreen
import com.superwakeupware.ui.theme.SuperWakeUpTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            SuperWakeUpTheme {
                SetAlarmScreen()
            }
        }
    }
}
