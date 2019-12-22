package com.infomantri.autosms.send.database.PhoneCall

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phone_call_alarm_table")
data class PhoneCallAlarm(
    val alarmTimeStamp: Long,

    var repeatAlarm: Boolean = false,

    @PrimaryKey(autoGenerate = true) var id: Int = 0
)