package com.infomantri.autosms.sender.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class SubscribersRepository (private val subscribersDao: SubscribersDao){

    val defaultMobileNo: LiveData<List<Subscribers>> = subscribersDao.getDefaultMobileNo()

    @WorkerThread
    fun insertMobileNo(mobileNo: Subscribers) {
        subscribersDao.insertMobileNo(mobileNo)
    }

}