package com.infomantri.autosms.send.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class MessageLiveDataRepository(private val msgLivaDataDao: MessageLivaDataDao, val id: Int = -1) {

    val allMessages: LiveData<List<Message>> = msgLivaDataDao.getAllMessages()
    val getSentMessages: LiveData<List<Message>> = msgLivaDataDao.getSentMessages()
    val getFailedMessages: LiveData<List<Message>> = msgLivaDataDao.getFailedMessages()
    val messageById: LiveData<Message> = msgLivaDataDao.getMessageById(id)

    @WorkerThread
    suspend fun insertMessage(msg: Message) {
        msgLivaDataDao.insertMessage(msg)
    }

    @WorkerThread
    fun deleteMessage(msg: Message) {
        msgLivaDataDao.delete(msg)
    }

}