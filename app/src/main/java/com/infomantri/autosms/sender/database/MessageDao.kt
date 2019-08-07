package com.infomantri.autosms.sender.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MessageDao {

    @Insert
    suspend fun insert(msg: Message)

    @Query("SELECT * from message_table ORDER BY messages ASC")
    fun getAllMessages(): LiveData<List<Message>>

    @Delete
    fun delete(message: Message)
}