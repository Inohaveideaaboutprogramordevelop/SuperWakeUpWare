package com.superwakeupware.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.superwakeupware.data.AlarmEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager,
) {
    companion object {
        const val EXTRA_ALARM_ID    = "alarm_id"
        const val EXTRA_ALARM_LABEL = "alarm_label"
        const val ACTION_FIRE       = "com.superwakeupware.ALARM_FIRE"
    }

    /** Schedule or re-schedule an alarm. Idempotent — cancels any prior intent with the same id. */
    fun schedule(alarm: AlarmEntity) {
        cancel(alarm)
        if (!alarm.enabled) return

        val triggerMs = nextTriggerMs(alarm)
        val intent = buildIntent(alarm)

        // setAlarmClock gives highest-priority delivery and shows clock icon in status bar.
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerMs, buildLaunchIntent()),
            intent,
        )
    }

    fun cancel(alarm: AlarmEntity) {
        alarmManager.cancel(buildIntent(alarm))
    }

    private fun buildIntent(alarm: AlarmEntity): PendingIntent {
        val i = Intent(ACTION_FIRE, null, context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_LABEL, alarm.label)
        }
        return PendingIntent.getBroadcast(
            context, alarm.id, i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun buildLaunchIntent(): PendingIntent {
        val i = context.packageManager.getLaunchIntentForPackage(context.packageName)!!
        return PendingIntent.getActivity(
            context, 0, i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun nextTriggerMs(alarm: AlarmEntity): Long {
        val now = Calendar.getInstance()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // If no repeat days, fire tomorrow if time already passed today.
        if (alarm.repeatDays == 0) {
            if (cal <= now) cal.add(Calendar.DAY_OF_YEAR, 1)
            return cal.timeInMillis
        }
        // Find the nearest matching weekday (Sunday=1 in Calendar, bit 0 in our mask).
        repeat(7) { offset ->
            val dayBit = 1 shl ((cal.get(Calendar.DAY_OF_WEEK) - 1 + offset) % 7)
            if (alarm.repeatDays and dayBit != 0) {
                cal.add(Calendar.DAY_OF_YEAR, if (offset == 0 && cal <= now) 7 else offset)
                return cal.timeInMillis
            }
        }
        return cal.timeInMillis
    }
}
