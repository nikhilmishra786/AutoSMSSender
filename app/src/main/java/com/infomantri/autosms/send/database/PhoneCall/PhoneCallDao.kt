package com.infomantri.autosms.send.database.PhoneCall

import androidx.lifecycle.LiveData
import androidx.room.*
import com.infomantri.autosms.send.database.PhoneCall.PhoneCallAlarm

@Dao
interface PhoneCallDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhoneCallAlarm(alarm: PhoneCallAlarm)

    @Query("SELECT * from phone_call_alarm_table")
    fun getAllPhoneCallAlarms(): LiveData<List<PhoneCallAlarm>>

    @Query("SELECT * from phone_call_alarm_table")
    fun getAllPhoneCallDbAlarms(): List<PhoneCallAlarm>

    @Query("SELECT * from phone_call_alarm_table WHERE id == :alarmId")
    fun getAlarmById(alarmId: Int): PhoneCallAlarm

    @Delete
    fun deletePhoneCallAlarm(alarm: PhoneCallAlarm)

}