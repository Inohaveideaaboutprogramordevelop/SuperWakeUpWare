package com.superwakeupware.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superwakeupware.alarm.AlarmScheduler
import com.superwakeupware.data.AlarmEntity
import com.superwakeupware.data.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetAlarmViewModel @Inject constructor(
    private val repo      : AlarmRepository,
    private val scheduler : AlarmScheduler,
) : ViewModel() {

    val alarms = repo.alarms.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addAlarm(hour: Int, minute: Int, label: String) = viewModelScope.launch {
        val alarm = AlarmEntity(hour = hour, minute = minute, label = label, enabled = true)
        val id = repo.upsert(alarm).toInt()
        scheduler.schedule(alarm.copy(id = id))
    }

    fun toggleAlarm(alarm: AlarmEntity) = viewModelScope.launch {
        val updated = alarm.copy(enabled = !alarm.enabled)
        repo.upsert(updated)
        if (updated.enabled) scheduler.schedule(updated) else scheduler.cancel(updated)
    }

    fun deleteAlarm(alarm: AlarmEntity) = viewModelScope.launch {
        scheduler.cancel(alarm)
        repo.delete(alarm)
    }
}
