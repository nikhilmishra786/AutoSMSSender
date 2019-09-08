package com.infomantri.autosms.send.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.infomantri.autosms.send.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddAlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val addAlarmLiveDataRepository: AddAlarmLiveDataRepository
    val allAlarms: LiveData<List<AddAlarm>>

    init {
        val addAlarmDao = MessageRoomDatabase.getDatabase(application).addAlarmLiveDataDao()
        addAlarmLiveDataRepository = AddAlarmLiveDataRepository(addAlarmDao)
        allAlarms = addAlarmLiveDataRepository.allAlarms
    }

    fun insert(alarm: AddAlarm) = viewModelScope.launch(Dispatchers.IO) {
        addAlarmLiveDataRepository.insertAlarm(alarm)
    }

    fun delete(alarm: AddAlarm) = viewModelScope.launch(Dispatchers.IO) {
        addAlarmLiveDataRepository.deleteAlarm(alarm)
    }
}