package com.infomantri.autosms.send.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PhoneCallDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhoneCallAlarm(alarm: PhoneCallAlarm)

    @Query("SELECT * from phone_call_alarm_table")
    fun getAllPhoneCallAlarms(): LiveData<List<PhoneCallAlarm>>

    @Query("SELECT * from phone_call_alarm_table WHERE id == :alarmId")
    fun getAlarmById(alarmId: Int): PhoneCallAlarm

    @Delete
    fun deletePhoneCallAlarm(alarm: PhoneCallAlarm)

}