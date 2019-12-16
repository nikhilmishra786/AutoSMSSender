package com.infomantri.autosms.send.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class PhoneCallRepository(val phoneCallDao: PhoneCallDao, alarmId: Int = -1) {

    val allPhoneCallAlarms: LiveData<List<PhoneCallAlarm>> =
        phoneCallDao.getAllPhoneCallAlarms()
    val alarmById: PhoneCallAlarm = phoneCallDao.getAlarmById(alarmId)

    @WorkerThread
    fun insertPhoneCallAlarm(alarm: PhoneCallAlarm) {
        phoneCallDao.insertPhoneCallAlarm(alarm)
    }

    @WorkerThread
    fun deletePhoneCallAlarm(alarm: PhoneCallAlarm) {
        phoneCallDao.deletePhoneCallAlarm(alarm)
    }
}