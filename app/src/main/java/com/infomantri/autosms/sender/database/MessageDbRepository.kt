package com.infomantri.autosms.sender.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class MessageDbRepository(private val msgDao: MessageDbDao) {

    val allMessages: List<Message> = msgDao.getAllMessages()

    @WorkerThread
    suspend fun insert(msg: Message) {
        msgDao.insert(msg)
    }

    @WorkerThread
    fun update(msg: Message){
        msgDao.update(msg)
    }

}