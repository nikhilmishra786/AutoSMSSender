package com.infomantri.autosms.send.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscribers_table")
data class Subscribers(
    @ColumnInfo(name = "subscribers_column")
    val mobileNo: String,

    val isSelected: Boolean = true,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)