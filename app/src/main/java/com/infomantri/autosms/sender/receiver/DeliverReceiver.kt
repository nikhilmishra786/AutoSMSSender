package com.infomantri.autosms.sender.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.infomantri.autosms.sender.activity.AddMessages
import com.infomantri.autosms.sender.activity.HomeActivity
import com.infomantri.autosms.sender.asynctask.BaseAsyncTask
import com.infomantri.autosms.sender.base.BaseActivity
import com.infomantri.autosms.sender.database.Message
import com.infomantri.autosms.sender.database.MessageDbRepository
import com.infomantri.autosms.sender.database.MessageRoomDatabase

class DeliverReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val pendingResult: PendingResult = goAsync()
        context?.let {
            Log.v("ASYNC_TASK", ">>> Before Async Task in Deliver onReceive()...")
            BaseActivity().sendSMS(context, true)
        }
    }

}