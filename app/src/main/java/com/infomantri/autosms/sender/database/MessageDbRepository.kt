package com.infomantri.autosms.sender.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class MessageDbRepository(private val msgDao: MessageDbDao) {

    val allMessages: List<Message> = msgDao.getAllMessages()

    @WorkerThread
    suspend fun insertMessage(msg: Message) {
        msgDao.insertMessage(msg)
    }

    @WorkerThread
    fun updateMessage(msg: Message){
        msgDao.updateMessage(msg)
    }

    val defaultMobileNo: List<Subscribers> = msgDao.getDefaultMobileNo()

    @WorkerThread
    fun insertMobileNo(mobileNo: Subscribers) {
        msgDao.insertMobileNo(mobileNo)
    }

}