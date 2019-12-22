package com.infomantri.autosms.send.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.infomantri.autosms.send.database.Message
import com.infomantri.autosms.send.database.MessageLiveDataRepository
import com.infomantri.autosms.send.database.MessageRoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageViewModel(application: Application) : AndroidViewModel(application) {

    private val liveDataRepository: MessageLiveDataRepository
    val allMessages: LiveData<List<Message>>
    var messageId: Int = -1
    val _getMsgById: LiveData<Message>
    val getSentMessages: LiveData<List<Message>>
    val getFailedMessages: LiveData<List<Message>>

    init {
        val msgsDao = MessageRoomDatabase.getDatabase(application).messageLiveDataDao()
        liveDataRepository = MessageLiveDataRepository(msgsDao, messageId)
        allMessages = liveDataRepository.allMessages
        _getMsgById = liveDataRepository.messageById
        getSentMessages = liveDataRepository.getSentMessages
        getFailedMessages = liveDataRepository.getFailedMessages
    }

    fun getMsgById(msgId: Int) {
        messageId = msgId
    }

    fun insert(msg: Message) = viewModelScope.launch(Dispatchers.IO) {
        liveDataRepository.insertMessage(msg)
    }

    fun delete(msg: Message) = viewModelScope.launch(Dispatchers.IO) {
        liveDataRepository.deleteMessage(msg)
    }
}