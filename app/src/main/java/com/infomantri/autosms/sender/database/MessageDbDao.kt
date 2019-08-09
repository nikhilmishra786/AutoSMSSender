package com.infomantri.autosms.sender.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MessageDbDao {

    @Insert
    suspend fun insert(msg: Message)

    @Query("SELECT * from message_table")
    fun getAllMessages(): List<Message>

    @Delete
    fun delete(message: Message)
}