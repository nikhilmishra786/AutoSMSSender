package com.infomantri.autosms.sender.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class MessageDbRepository(private val msgDao: MessageDbDao, val id: Int = -1) {

    val allMessages: List<Message> = msgDao.getAllMessages()
    val messageById: Message = msgDao.getMessageById(id)
    val messageByTimeStamp: Message = msgDao.getMessageByTimeStamp(id)

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

    @WorkerThread
    fun deleteMessage(msg: Message) {
        msgDao.deleteMessage(msg)
    }

}