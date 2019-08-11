package com.infomantri.autosms.sender.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SubscribersDao {

    @Insert
    fun insertMobileNo(mobileNo: Subscribers)

    @Query("SELECT * from subscribers_table WHERE isDefault == 1")
    fun getDefaultMobileNo(): List<Subscribers>

    @Delete
    fun delete(message: Message)
}