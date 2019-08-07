package com.infomantri.autosms.sender.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.infomantri.autosms.sender.database.Message
import com.infomantri.autosms.sender.database.MessageRepository
import com.infomantri.autosms.sender.database.MessageRoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MessageRepository
    val allMessages: LiveData<List<Message>>

    init {
        val msgsDao = MessageRoomDatabase.getDatabase(application).messageDao()
        repository = MessageRepository(msgsDao)
        allMessages = repository.allMessages
    }

    fun insert(msg: Message) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(msg)
    }
}