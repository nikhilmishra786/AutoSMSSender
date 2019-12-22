package com.infomantri.autosms.send.database.PhoneCall

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.infomantri.autosms.send.database.PhoneCall.PhoneCallAlarm
import com.infomantri.autosms.send.database.PhoneCall.PhoneCallDao

class PhoneCallRepository(val phoneCallDao: PhoneCallDao, alarmId: Int = -1) {

    val allPhoneCallAlarms: LiveData<List<PhoneCallAlarm>> =
        phoneCallDao.getAllPhoneCallAlarms()
    val allPhoneCallDbAlarms: List<PhoneCallAlarm> = phoneCallDao.getAllPhoneCallDbAlarms()
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