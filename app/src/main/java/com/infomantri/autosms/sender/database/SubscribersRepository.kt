package com.infomantri.autosms.sender.database

import androidx.annotation.WorkerThread

class SubscribersRepository (private val subscribersDao: SubscribersDao){

    val defaultMobileNo: List<Subscribers> = subscribersDao.getDefaultMobileNo()

    @WorkerThread
    fun insertMobileNo(mobileNo: Subscribers) {
        subscribersDao.insertMobileNo(mobileNo)
    }

}