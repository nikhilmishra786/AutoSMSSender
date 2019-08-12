package com.infomantri.autosms.sender.database

import androidx.room.*

@Dao
interface MessageDbDao {

    @Insert
    suspend fun insert(msg: Message)

    @Update
    fun update(msgId: Message)

    @Query("SELECT * from message_table")
    fun getAllMessages(): List<Message>

    @Delete
    fun delete(message: Message)
}