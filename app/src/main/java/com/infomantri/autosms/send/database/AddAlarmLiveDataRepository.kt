package com.infomantri.autosms.send.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class AddAlarmLiveDataRepository(private val addAlarmLiveDataDao: AddAlarmLiveDataDao) {

    val allAlarms: LiveData<List<AddAlarm>> = addAlarmLiveDataDao.getAllAlarms()
    val allPhoneCallAlarms: LiveData<List<PhoneCallAlarm>> =
        addAlarmLiveDataDao.getAllPhoneCallAlarms()

    @WorkerThread
    fun insertAlarm(alarm: AddAlarm) {
        addAlarmLiveDataDao.insertAlarm(alarm)
    }

    @WorkerThread
    fun insertPhoneCallAlarm(alarm: PhoneCallAlarm) {
        addAlarmLiveDataDao.insertPhoneCallAlarm(alarm)
    }

    @WorkerThread
    fun deleteAlarm(alarm: AddAlarm) {
        addAlarmLiveDataDao.delete(alarm)
    }

    @WorkerThread
    fun deletePhoneCallAlarm(alarm: PhoneCallAlarm) {
        addAlarmLiveDataDao.deletePhoneCallAlarm(alarm)
    }
}