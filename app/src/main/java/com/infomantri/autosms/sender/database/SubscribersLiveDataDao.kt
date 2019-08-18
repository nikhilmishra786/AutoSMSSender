package com.infomantri.autosms.sender.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SubscribersLiveDataDao {

    @Insert
    fun insertMobileNo(mobileNo: Subscribers)

    @Query("SELECT * from subscribers_table")
    fun getAllMobileNo(): LiveData<List<Subscribers>>

    @Query("SELECT * from subscribers_table WHERE isSelected == 1")
    fun getDefaultMobileNo(): LiveData<Subscribers>

    @Query("SELECT * from subscribers_table where isSelected == 0")
    fun getNonDefaultNo(): LiveData<Subscribers>

    @Update
    fun updateMobileNo(mobileNo: Subscribers)

    @Delete
    fun delete(message: Message)
}