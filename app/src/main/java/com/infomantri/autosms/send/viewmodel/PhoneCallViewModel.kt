package com.infomantri.autosms.send.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.infomantri.autosms.send.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhoneCallViewModel(application: Application) : AndroidViewModel(application) {

    private val phoneCallRepository: PhoneCallRepository
    val allPhoneCallAlarms: LiveData<List<PhoneCallAlarm>>

    init {
        val phoneCallDao = MessageRoomDatabase.getDatabase(application).phoneCallAlarmDao()
        phoneCallRepository = PhoneCallRepository(phoneCallDao)
        allPhoneCallAlarms = phoneCallRepository.allPhoneCallAlarms
    }

    fun insertPhoneCallAlarm(alarm: PhoneCallAlarm) = viewModelScope.launch(Dispatchers.IO) {
        phoneCallRepository.insertPhoneCallAlarm(alarm)
    }

    fun deletePhoneCallAlarm(alarm: PhoneCallAlarm) = viewModelScope.launch(Dispatchers.IO) {
        phoneCallRepository.deletePhoneCallAlarm(alarm)
    }
}