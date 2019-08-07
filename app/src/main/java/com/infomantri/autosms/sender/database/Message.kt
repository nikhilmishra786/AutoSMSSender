package com.infomantri.autosms.sender.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_table")
data class Message(
    @ColumnInfo(name = "messages") val message: String,
    @PrimaryKey(autoGenerate = true) val slNo: Int = 0)