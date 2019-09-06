package com.infomantri.autosms.send.database

import androidx.annotation.WorkerThread

class MessageDbRepository(private val msgDao: MessageDbDao, val id: Int = -1, val msg: String = "") {

    val allMessages: List<Message> = msgDao.getAllMessages()
    val messageById: Message = msgDao.getMessageById(id)
    val messageByTimeStamp: Message = msgDao.getMessageByTimeStamp(msg)

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