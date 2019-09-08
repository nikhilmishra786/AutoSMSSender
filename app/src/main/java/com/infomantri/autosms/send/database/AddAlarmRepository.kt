package com.infomantri.autosms.send.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class AddAlarmRepository(private val addAlarmDao: AddAlarmDao, val alarmId: Int = -1) {

    val allAlarms: List<AddAlarm> = addAlarmDao.getAllAlarms()
    val alarmById: AddAlarm = addAlarmDao.getAlarmById(alarmId)

    @WorkerThread
    fun insertAlarm(alarm: AddAlarm) {
        addAlarmDao.insertAlarm(alarm)
    }

    @WorkerThread
    fun deleteAlarm(alarm: AddAlarm) {
        addAlarmDao.delete(alarm)
    }
}