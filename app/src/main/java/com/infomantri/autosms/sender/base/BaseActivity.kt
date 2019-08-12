package com.infomantri.autosms.sender.base

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.infomantri.autosms.sender.asynctask.BaseAsyncTask
import com.infomantri.autosms.sender.database.MessageDbRepository
import com.infomantri.autosms.sender.database.MessageRepository
import com.infomantri.autosms.sender.database.MessageRoomDatabase

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState)

    }

    companion object fun getFromDatabase(context: Context) : MessageDbRepository{

            val msgDao = MessageRoomDatabase.getDatabase(context).messageDbDao()
            val repository = MessageDbRepository(msgDao)

        return repository
    }

}