package com.infomantri.autosms.send.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AddAlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlarm(alarm: AddAlarm)

    @Query("SELECT * from add_alarm_table")
    fun getAllAlarms(): List<AddAlarm>

    @Query("SELECT * from add_alarm_table WHERE id == :alarmId")
    fun getAlarmById(alarmId: Int): AddAlarm

    @Delete
    fun delete(alarm: AddAlarm)
}