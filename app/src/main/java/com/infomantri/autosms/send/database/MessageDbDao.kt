package com.infomantri.autosms.send.database

import androidx.room.*

@Dao
interface MessageDbDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: Message)

    @Update
    fun updateMessage(msgId: Message)

    @Query("SELECT * from message_table")
    fun getAllMessages(): List<Message>

    @Query("SELECT * from message_table WHERE id = :mId")
    fun getMessageById(mId: Int): Message

    @Query("SELECT * from message_table WHERE message = :mMsg")
    fun getMessageByTimeStamp(mMsg: String): Message

    @Query("SELECT * from message_table where sent == 1")
    fun getSentMessages(): List<Message>

    @Delete
    fun deleteMessage(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMobileNo(mobileNo: Subscribers)

    @Query("SELECT * from subscribers_table WHERE isSelected == 1")
    fun getDefaultMobileNo(): List<Subscribers>

    @Delete
    fun deleteSubscriber(message: Message)
}