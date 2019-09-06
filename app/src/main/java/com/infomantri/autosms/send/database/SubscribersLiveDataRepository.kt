package com.infomantri.autosms.send.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class SubscribersLiveDataRepository (private val subscribersLiveDataDao: SubscribersLiveDataDao){

    val allMobileNo: LiveData<List<Subscribers>> = subscribersLiveDataDao.getAllMobileNo()
    val defaultMobileNo: LiveData<Subscribers> = subscribersLiveDataDao.getDefaultMobileNo()
    val nonDefaultNo: LiveData<Subscribers> = subscribersLiveDataDao.getNonDefaultNo()

    @WorkerThread
    fun insertMobileNo(mobileNo: Subscribers) {
        subscribersLiveDataDao.insertMobileNo(mobileNo)
    }

    @WorkerThread
    fun updateMobileNo(mobileNo: Subscribers) {
        subscribersLiveDataDao.updateMobileNo(mobileNo)
    }

}