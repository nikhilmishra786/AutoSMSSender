package com.infomantri.autosms.sender.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.infomantri.autosms.sender.database.Message
import com.infomantri.autosms.sender.database.MessageLiveDataRepository
import com.infomantri.autosms.sender.database.MessageRoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageViewModel(application: Application) : AndroidViewModel(application) {

    private val liveDataRepository: MessageLiveDataRepository
    val allMessages: LiveData<List<Message>>
    val getSentMsgCount: LiveData<List<Message>>

    init {
        val msgsDao = MessageRoomDatabase.getDatabase(application).messageLiveDataDao()
        liveDataRepository = MessageLiveDataRepository(msgsDao)
        allMessages = liveDataRepository.allMessages
        getSentMsgCount = liveDataRepository.getSentMsgCount
    }

    fun insert(msg: Message) = viewModelScope.launch(Dispatchers.IO) {
        liveDataRepository.insertMessage(msg)
    }

    fun delete(msg: Message) = viewModelScope.launch(Dispatchers.IO) {
        liveDataRepository.deleteMessage(msg)
    }
}