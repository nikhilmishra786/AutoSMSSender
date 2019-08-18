package com.infomantri.autosms.sender.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.infomantri.autosms.sender.asynctask.BaseAsyncTask
import com.infomantri.autosms.sender.database.MessageDbRepository
import com.infomantri.autosms.sender.database.MessageRoomDatabase

class SentReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        context?.let {
            when(resultCode){

                Activity.RESULT_OK -> {
                    toast(context, "Message sent successfully...", false)
                }

                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                    toast(context,"Sms Error Generic Failure...")
                }

                SmsManager.RESULT_ERROR_NO_SERVICE -> {
                    toast(context, "Sms Error No Service")
                }

                SmsManager.RESULT_ERROR_NULL_PDU -> {
                    toast(context, "Sms Error Null PDU")
                }

                SmsManager.RESULT_ERROR_RADIO_OFF -> {
                    toast(context, "Sms Error Radio Off")
                }
            }
        }
    }

    private fun toast(context: Context?, msg: String, isSentFailed: Boolean = true) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        Log.v("SMS_SENT_OnReceive", msg)
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("IS_SENT_ERROR", isSentFailed).apply()
    }
}