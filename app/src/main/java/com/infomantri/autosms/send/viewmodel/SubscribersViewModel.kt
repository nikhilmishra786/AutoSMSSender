package com.infomantri.autosms.send.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.infomantri.autosms.send.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.infomantri.autosms.send.database.SubscribersLiveDataRepository

class SubscribersViewModel(application: Application) : AndroidViewModel(application) {

    private val liveDataRepository: SubscribersLiveDataRepository
    val allMobileNo: LiveData<List<Subscribers>>
    val defaultMobileNo: LiveData<Subscribers>
    val nonDefaultNo: LiveData<Subscribers>

    init {
        val subscribersDao = MessageRoomDatabase.getDatabase(application).subscribersDao()
        liveDataRepository = SubscribersLiveDataRepository(subscribersDao)
        allMobileNo = liveDataRepository.allMobileNo
        defaultMobileNo = liveDataRepository.defaultMobileNo
        nonDefaultNo = liveDataRepository.nonDefaultNo
    }

    fun insert(mobileNo: Subscribers) = viewModelScope.launch(Dispatchers.IO) {
        liveDataRepository.insertMobileNo(mobileNo)
    }
}