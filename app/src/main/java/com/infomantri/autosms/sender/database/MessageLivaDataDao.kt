package com.infomantri.autosms.sender.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MessageLivaDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: Message)

    @Query("SELECT * from message_table")
    fun getAllMessages(): LiveData<List<Message>>

    @Delete
    fun delete(message: Message)
}