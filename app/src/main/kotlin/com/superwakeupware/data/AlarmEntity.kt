package com.superwakeupware.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String = "",
    val hour: Int,
    val minute: Int,
    /** Bitmask: Sun=1, Mon=2, Tue=4, Wed=8, Thu=16, Fri=32, Sat=64. 0 = one-shot. */
    val repeatDays: Int = 0,
    val enabled: Boolean = true,
    /** Tone URI as string; null = default ringtone. */
    val ringtoneUri: String? = null,
)
