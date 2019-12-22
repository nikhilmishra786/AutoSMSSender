package com.infomantri.autosms.send.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import com.infomantri.autosms.send.SmsSenderApp
import com.infomantri.autosms.send.activity.AddAlarmsActivity
import com.infomantri.autosms.send.activity.HomeActivity
import com.infomantri.autosms.send.constants.AppConstant
import com.infomantri.autosms.send.database.MessageRoomDatabase
import com.infomantri.autosms.send.database.PhoneCall.PhoneCallRepository
import com.infomantri.autosms.send.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class ConfirmationSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            Log.v("ConfirmationSmsReceiver", ">>> ConfirmationSmsReceiver onReceive()...")
            val mJob = Job()
            CoroutineScope(Dispatchers.Default + mJob).launch {
                val phoneCallDao = MessageRoomDatabase.getDatabase(context).phoneCallAlarmDao()
                val repository =
                    PhoneCallRepository(
                        phoneCallDao
                    )
                val calendar = Calendar.getInstance()
                val phoneAlarmCalendar = Calendar.getInstance()
                var message = "Your Alarms are Confirmed..."
                var count = 0

                repository.allPhoneCallDbAlarms.iterator().forEach { phoneCallAlarm ->
                    phoneCallAlarm.alarmTimeStamp.formatTime()?.let {
                        phoneAlarmCalendar.timeInMillis = phoneCallAlarm.alarmTimeStamp
                        calendar.apply {
                            set(Calendar.HOUR_OF_DAY, phoneAlarmCalendar.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, phoneAlarmCalendar.get(Calendar.MINUTE))
                            set(Calendar.SECOND, 0)
                        }
                        context.setDozeModeAlarm(
                            calendar,
                            phoneCallAlarm.alarmTimeStamp.toInt() * 1000,
                            "Doze Mode Alarm Fired"
                        )
                        message = message.plus(" $it, ")
                        Log.v(
                            "ConfirmationAlarm",
                            ">>> ConfirmationAlarm setDoze Alarm: ${phoneCallAlarm.alarmTimeStamp.formatTime()} Message: $message"
                        )
                        count++
                    }
                }
                sendSMS(context, message)
                sendNotification(
                    context,
                    System.currentTimeMillis().toInt(),
                    "Confirmation Message Alarm Fired!",
                    "All Alarms Status send Successfully... $count Alarms",
                    AddAlarmsActivity::class.java,
                    AppConstant.Notification.Channel.MESSAGE_CONFIRMATION_CHANNEL_ID,
                    AppConstant.Notification.Channel.MESSAGE_CONFIRMATION_CHANNEL
                )
                context.startSmsRetriever()
                if (context.getBooleanFromPreference(AppConstant.IS_AUTO_STARTUP) == true) {
                    context.startActivity(
                        Intent(
                            context,
                            HomeActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
        }
    }
}

fun sendSMS(
    context: Context, message: String
) {
    val mJob = Job()
    CoroutineScope(Dispatchers.Default + mJob).launch {
        val smsManager = SmsManager.getDefault() as SmsManager

        val DEFAULT_MOBILE_NO =
            context.getStringFromPreference(AppConstant.DEFAULT_MOBILE_NO)

        try {

            val sentIntent = Intent(context, SentReceiver::class.java).apply {
                putExtra(AppConstant.MESSAGE, message)
                putExtra(
                    AppConstant.Reminder.TIME_STAMP,
                    System.currentTimeMillis()
                )
                putExtra(AppConstant.Reminder.REMINDER_ID, System.currentTimeMillis().toInt())
                putExtra(AppConstant.Reminder.TITLE, "Message Sent Successfully...")
            }
            sentIntent.action = AppConstant.MESSAGE_SENT

            val sentPendingIntent = PendingIntent.getBroadcast(
                context, 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            val deliveredIntent =
                Intent(context, DeliverReceiver::class.java).apply {
                    action = AppConstant.Intent.ACTION_CONFIRMATION_SMS
                }

            val deliveredPendingIntent = PendingIntent.getBroadcast(
                context, 0, deliveredIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            smsManager.sendTextMessage(
                DEFAULT_MOBILE_NO,
                null,
                message,
                sentPendingIntent,
                deliveredPendingIntent
            )


        } catch (e: Exception) {

            Log.v(
                "SEND_SMS_Error!...",
                ">>> (inside SmsReceiver) Error While Sending SMS... $e"
            )
        }
    }
}