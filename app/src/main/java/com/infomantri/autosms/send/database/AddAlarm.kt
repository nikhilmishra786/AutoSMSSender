package com.infomantri.autosms.send.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "add_alarm_table")
data class AddAlarm (

    val alarmTimeStamp: Long,

    var repeatAlarm: Boolean = false,

    @PrimaryKey(autoGenerate = true) var id: Int = 0)
