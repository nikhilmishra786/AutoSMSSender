package com.infomantri.autosms.sender.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Entity(tableName = "message_table")
data class Message(
    @ColumnInfo(name = "messages_column")
    var message: String,

    var timeStamp: Long,

    var sent: Boolean,

    var isFailed: Boolean = false,

    var sentCount: Int = 0,

    @PrimaryKey(autoGenerate = true) var id: Int = 0)


// Here we have used Parcelable with @Parcelise Annotation + Main thing is to Add Apply{ Extentions } in App.Build
//apply plugin: 'org.jetbrains.kotlin.android.extensions'
//
//android {
//    androidExtensions {
//        experimental = true
//    }
//}