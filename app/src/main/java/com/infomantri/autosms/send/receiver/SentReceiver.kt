package com.infomantri.autosms.send.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.telephony.SmsManager
import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import com.infomantri.autosms.send.activity.HomeActivity
import com.infomantri.autosms.send.constants.AppConstant
import com.infomantri.autosms.send.database.MessageDbRepository
import com.infomantri.autosms.send.database.MessageRoomDatabase
import com.infomantri.autosms.send.util.setBooleanFromPreference
import java.text.SimpleDateFormat
import java.util.*

class SentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val pendingResult: PendingResult = goAsync()

        Log.v("Sent_onReceive", ">>> Sent onRecive() .....")
        val reminderId = intent?.getIntExtra(AppConstant.Reminder.REMINDER_ID, 1000) ?: 1111
        val title = intent?.getStringExtra(AppConstant.Reminder.TITLE) ?: "title: "
        val timeStamp = intent?.getLongExtra(AppConstant.Reminder.TIME_STAMP, -1) ?: "timeStamp: "
        val subTitle = "Message sent at : ${SimpleDateFormat(
            "dd-MMM-yyyy hh:mm a",
            Locale.US
        ).format(timeStamp)}"
//        val msgId = intent?.getIntExtra(AppConstant.MESSAGE_ID, -1) ?: -1

//        Log.v(
//            "Intent_Extras_Sent",
//            ">>> intent extras values: reminderId: $reminderId title: $title timeStamp: $timeStamp subTitle: $subTitle msgId: $msgId"
//        )

        context?.let {
            Log.v(
                "ASYNC_TASK",
                ">>> Before Async Task in Sent onReceive()... resultCode: $resultCode }"
            )

            when (resultCode) {

                Activity.RESULT_OK -> {
                    toast(context, "Message sent successfully...", false)
                    sendNotification(
                        context,
                        reminderId,
                        title,
                        "Message Sent Successfully...",
                        HomeActivity::class.java
                    )
                }

                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                    toast(context, "Sms Error Generic Failure...")
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

                Activity.RESULT_CANCELED -> {
                    Log.v(
                        "RESULT_CANCELED",
                        ">>> STATUS_ON_ICC_SENT: ${SmsManager.STATUS_ON_ICC_SENT}...->"
                    )

                    toast(context, "Message sent successfully...", false)
                    sendNotification(
                        context,
                        reminderId,
                        title,
                        "CANCELED sent ",
                        HomeActivity::class.java
                    )
                }

                else -> {

                }

            }
        }
    }
}

private fun toast(context: Context?, msg: String, isSentFailed: Boolean = true) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    Log.v("SMS_SENT_OnReceive", msg)
    context?.setBooleanFromPreference(AppConstant.Error.SENT_ERROR, isSentFailed)
}