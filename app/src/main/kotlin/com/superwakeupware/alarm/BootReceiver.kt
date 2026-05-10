package com.superwakeupware.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.superwakeupware.data.AlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Re-schedules all enabled alarms after a device reboot. */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var repo      : AlarmRepository
    @Inject lateinit var scheduler : AlarmScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in listOf(
                Intent.ACTION_BOOT_COMPLETED,
                "android.intent.action.LOCKED_BOOT_COMPLETED"
            )
        ) return

        scope.launch {
            repo.alarms.first()
                .filter { it.enabled }
                .forEach { scheduler.schedule(it) }
        }
    }
}
