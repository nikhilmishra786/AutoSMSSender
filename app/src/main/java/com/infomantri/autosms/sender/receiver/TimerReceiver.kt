package com.infomantri.autosms.sender.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import com.infomantri.autosms.sender.AddNewMessages
import kotlinx.android.synthetic.main.activity_new_message.*
import java.lang.Exception

class TimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        Log.e("REMINDER", ">>> Reminder recevied 1 ->>>>")
        AddNewMessages().sendSMS()
        Log.e("REMINDER", ">>> Reminder recevied 2 ->>>>")
        playRingtone(context)
        Log.e("REMINDER", ">>> Reminder recevied 3 ->>>>")
        Toast.makeText(context, "REMINDER", Toast.LENGTH_LONG).show()
        Log.e("REMINDER", ">>> Reminder recevied 4 ->>>>")
    }

    private fun playRingtone(context: Context?) {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val ringtone = RingtoneManager.getRingtone(context, uri)
        ringtone.play()
    }
//
//    private fun sendSMS() {
//        try{
//            val smsManager = SmsManager.getDefault() as SmsManager
//            smsManager.sendTextMessage(etEnterPhoneNo.text.toString(),null,etEnterMsg.text.toString(),null,null)
//            Toast.makeText(this@AddNewMessages, "SMS Sent Successfully", Toast.LENGTH_SHORT).show()
//        }
//        catch (e: Exception){
//            Toast.makeText(this@AddNewMessages, "SMS Failed to Send, Please try again...", Toast.LENGTH_SHORT).show()
//        }
//    }
}