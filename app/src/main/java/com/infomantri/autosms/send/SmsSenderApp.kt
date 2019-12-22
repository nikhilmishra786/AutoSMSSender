package com.infomantri.autosms.send

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.infomantri.autosms.send.constants.AppConstant
import com.infomantri.autosms.send.receiver.ConfirmationSmsReceiver
import com.infomantri.autosms.send.util.formatDate
import com.infomantri.autosms.send.util.formatTime
import com.infomantri.autosms.send.util.getAlarmTitle
import com.infomantri.autosms.send.util.startSmsRetriever
import java.util.*

class SmsSenderApp : Application() {

    companion object {
        lateinit var mApplication: SmsSenderApp
        val CHANNEL_ID = "ExampleChannel"
        val REMINDER_CHANNEL_ID = "ReminderChannel"
        val NON_SMART_PUMP_CHANNEL_ID = "NonSmartPumpChannel"

        fun applicationContext(): Context {
            return mApplication.applicationContext
        }
    }

    init {
        mApplication = this
    }

    override fun onCreate() {
        super.onCreate()
        createNotification()
        startSmsRetriever()
    }

    private fun createNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val msgAlarmReminder = NotificationChannel(
                AppConstant.Notification.Channel.MESSAGE_CHANNEL_ID,
                AppConstant.Notification.Channel.MESSAGE_CHANNEL,
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(msgAlarmReminder)

            val msgConfirmationReminder = NotificationChannel(
                AppConstant.Notification.Channel.MESSAGE_CONFIRMATION_CHANNEL_ID,
                AppConstant.Notification.Channel.MESSAGE_CONFIRMATION_CHANNEL,
                NotificationManager.IMPORTANCE_HIGH
            )

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(msgConfirmationReminder)

            val phoneCallAlarmReminder = NotificationChannel(
                AppConstant.Notification.Channel.PHONE_CALL_CHANNEL_ID,
                AppConstant.Notification.Channel.PHONE_CALL_CHANNEL,
                NotificationManager.IMPORTANCE_HIGH
            )

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(phoneCallAlarmReminder)
        }
    }
}