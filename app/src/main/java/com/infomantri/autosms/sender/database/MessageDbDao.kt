package com.infomantri.autosms.sender.database

import androidx.room.*

@Dao
interface MessageDbDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: Message)

    @Update
    fun updateMessage(msgId: Message)

    @Query("SELECT * from message_table")
    fun getAllMessages(): List<Message>

    @Delete
    fun deleteMessage(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMobileNo(mobileNo: Subscribers)

    @Query("SELECT * from subscribers_table WHERE isSelected == 1")
    fun getDefaultMobileNo(): List<Subscribers>

    @Delete
    fun deleteSubscriber(message: Message)
}