package com.infomantri.autosms.send.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.infomantri.autosms.send.database.PhoneCall.PhoneCallAlarm

@Dao
interface AddAlarmLiveDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlarm(alarm: AddAlarm)

    @Query("SELECT * from add_alarm_table")
    fun getAllAlarms(): LiveData<List<AddAlarm>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhoneCallAlarm(alarm: PhoneCallAlarm)

    @Query("SELECT * from phone_call_alarm_table")
    fun getAllPhoneCallAlarms(): LiveData<List<PhoneCallAlarm>>

//    @Query("SELECT * from add_alarm_table")
//    fun getFutureReminder(): LiveData<List<AddAlarm>>

    @Delete
    fun delete(alarm: AddAlarm)

    @Delete
    fun deletePhoneCallAlarm(alarm: PhoneCallAlarm)

}