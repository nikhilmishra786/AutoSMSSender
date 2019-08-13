package com.infomantri.autosms.sender.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "max_sent_limit")
class MaxSentLimit (
    @ColumnInfo(name = "messages_column")
    var maxSentLimit: Int = 2,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)