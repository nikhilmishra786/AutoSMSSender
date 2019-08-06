package com.infomantri.autosms.sender.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class MessageRepository(private val msgDao: MessageDao) {

    val allMessages: LiveData<List<Message>> = msgDao.getAllMessages()

    @WorkerThread
    suspend fun insert(msg: Message) {
        msgDao.insert(msg)
    }
}