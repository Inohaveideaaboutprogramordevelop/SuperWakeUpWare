package com.superwakeupware.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.superwakeupware.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Foreground Service that:
 *  1. Acquires a partial wake lock so the CPU stays alive.
 *  2. Plays the alarm ringtone (looping).
 *  3. Launches AlarmActivity as a full-screen Intent.
 *  4. Exposes volume control to AlarmActivity via [increaseVolume].
 *
 * The service stops only when AlarmActivity calls [stopSelf] after a
 * microgame victory — ensuring the alarm cannot be killed by swiping the app.
 */
@AndroidEntryPoint
class AlarmService : Service() {

    companion object {
        const val CHANNEL_ID        = "alarm_channel"
        const val NOTIF_ID          = 1001
        const val VOLUME_STEP       = 0.10f   // +10 % per failed microgame
        var instance: AlarmService? = null    // safe single-instance reference for Activity
    }

    @Inject lateinit var notificationManager: NotificationManager
    @Inject lateinit var powerManager: PowerManager

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var currentVolume = 0.4f     // start at 40 % — escalates on failure

    override fun onCreate() {
        super.onCreate()
        instance = this
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId    = intent?.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1) ?: -1
        val alarmLabel = intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "Wake Up!"

        acquireWakeLock()
        startForeground(NOTIF_ID, buildNotification(alarmLabel, alarmId))
        startRingtone()
        launchAlarmActivity(alarmId, alarmLabel)

        return START_STICKY
    }

    // Called by AlarmActivity each time the user fails a microgame.
    fun increaseVolume() {
        currentVolume = (currentVolume + VOLUME_STEP).coerceAtMost(1f)
        mediaPlayer?.setVolume(currentVolume, currentVolume)
    }

    // Called by AlarmActivity after microgame victory.
    fun dismissAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        wakeLock?.release()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        instance = null
    }

    private fun startRingtone() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setLegacyStreamType(AudioManager.STREAM_ALARM)
                    .build()
            )
            setDataSource(applicationContext, uri)
            isLooping = true
            setVolume(currentVolume, currentVolume)
            prepare()
            start()
        }
    }

    private fun launchAlarmActivity(alarmId: Int, label: String) {
        val i = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, label)
        }
        startActivity(i)
    }

    private fun acquireWakeLock() {
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SuperWakeUp::AlarmLock"
        ).also { it.acquire(10 * 60 * 1000L) }  // max 10 min safety cap
    }

    private fun buildNotification(label: String, alarmId: Int): Notification {
        val fullScreenIntent = PendingIntent.getActivity(
            this, alarmId,
            Intent(this, AlarmActivity::class.java).apply {
                putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, label)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("⏰ $label")
            .setContentText("Complete the microgame to dismiss!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenIntent, true)
            .setOngoing(true)
            .build()
    }

    private fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alarms",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Super Wake-Up Ware alarm notifications"
            setSound(null, null)   // sound is handled by MediaPlayer, not the notification
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }
}
