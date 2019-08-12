package com.infomantri.autosms.sender.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "message_table")
data class Message(
    @ColumnInfo(name = "messages_column")
    val message: String,

    val timeStamp: Long,

    var sent: Boolean,

    var isFailed: Boolean = false,

    var sentCount: Int = 0,

    @PrimaryKey(autoGenerate = true) val id: Int = 0)