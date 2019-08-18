package com.infomantri.autosms.sender.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager

class MessageLiveDataRepository(private val msgLivaDataDao: MessageLivaDataDao) {

    val allMessages: LiveData<List<Message>> = msgLivaDataDao.getAllMessages()
    val getSentMsgCount: LiveData<List<Message>> = msgLivaDataDao.getSentMsgCount()

    @WorkerThread
    suspend fun insertMessage(msg: Message) {
        msgLivaDataDao.insertMessage(msg)
    }

    @WorkerThread
    fun deleteMessage(msg: Message) {
        msgLivaDataDao.delete(msg)
    }

}