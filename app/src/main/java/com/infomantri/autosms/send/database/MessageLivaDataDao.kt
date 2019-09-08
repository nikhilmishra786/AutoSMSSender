package com.infomantri.autosms.send.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MessageLivaDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: Message)

    @Query("SELECT * from message_table")
    fun getAllMessages(): LiveData<List<Message>>

    @Query("SELECT * from message_table WHERE id = :mId")
    fun getMessageById(mId: Int): LiveData<Message>

    @Query("SELECT * from message_table where sent == 1")
    fun getSentMsgCount(): LiveData<List<Message>>

    @Delete
    fun delete(message: Message)
}