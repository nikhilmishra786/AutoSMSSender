package com.infomantri.autosms.send.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AddAlarmLiveDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlarm(alarm: AddAlarm)

    @Query("SELECT * from add_alarm_table")
    fun getAllAlarms(): LiveData<List<AddAlarm>>

//    @Query("SELECT * from add_alarm_table")
//    fun getFutureReminder(): LiveData<List<AddAlarm>>

    @Delete
    fun delete(alarm: AddAlarm)

}