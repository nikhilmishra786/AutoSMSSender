package com.infomantri.autosms.send.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.infomantri.autosms.send.database.*
import com.infomantri.autosms.send.database.PhoneCall.PhoneCallAlarm
import com.infomantri.autosms.send.database.PhoneCall.PhoneCallRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddAlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val addAlarmLiveDataRepository: AddAlarmLiveDataRepository
    val allAlarms: LiveData<List<AddAlarm>>

    private val phoneCallRepository: PhoneCallRepository
    val allPhoneCallAlarms: LiveData<List<PhoneCallAlarm>>

    init {
        val addAlarmDao = MessageRoomDatabase.getDatabase(application).addAlarmLiveDataDao()
        addAlarmLiveDataRepository = AddAlarmLiveDataRepository(addAlarmDao)
        allAlarms = addAlarmLiveDataRepository.allAlarms

        val phoneCallDao = MessageRoomDatabase.getDatabase(application).phoneCallAlarmDao()
        phoneCallRepository =
            PhoneCallRepository(
                phoneCallDao
            )
        allPhoneCallAlarms = phoneCallRepository.allPhoneCallAlarms
    }

    fun insert(alarm: AddAlarm) = viewModelScope.launch(Dispatchers.IO) {
        addAlarmLiveDataRepository.insertAlarm(alarm)
    }

    fun delete(alarm: AddAlarm) = viewModelScope.launch(Dispatchers.IO) {
        addAlarmLiveDataRepository.deleteAlarm(alarm)
    }

    fun insertPhoneCallAlarm(alarm: PhoneCallAlarm) = viewModelScope.launch(Dispatchers.IO) {
        phoneCallRepository.insertPhoneCallAlarm(alarm)
    }

    fun deletePhoneCallAlarm(alarm: PhoneCallAlarm) = viewModelScope.launch(Dispatchers.IO) {
        phoneCallRepository.deletePhoneCallAlarm(alarm)
    }
}