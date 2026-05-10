package com.superwakeupware.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(private val dao: AlarmDao) {

    val alarms: Flow<List<AlarmEntity>> = dao.observeAll()

    suspend fun upsert(alarm: AlarmEntity): Long = dao.upsert(alarm)

    suspend fun delete(alarm: AlarmEntity) = dao.delete(alarm)

    suspend fun setEnabled(id: Int, enabled: Boolean) = dao.setEnabled(id, enabled)

    suspend fun getById(id: Int): AlarmEntity? = dao.getById(id)
}
