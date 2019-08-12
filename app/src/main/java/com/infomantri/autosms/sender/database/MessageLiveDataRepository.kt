package com.infomantri.autosms.sender.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class MessageLiveDataRepository(private val msgLivaDataDao: MessageLivaDataDao) {

    val allMessages: LiveData<List<Message>> = msgLivaDataDao.getAllMessages()

    @WorkerThread
    suspend fun insertMessage(msg: Message) {
        msgLivaDataDao.insertMessage(msg)
    }
}