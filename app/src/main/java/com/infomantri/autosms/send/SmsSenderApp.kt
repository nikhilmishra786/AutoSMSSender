package com.infomantri.autosms.send

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class SmsSenderApp: Application() {

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
    }

    private fun createNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmReminder = NotificationChannel(
                CHANNEL_ID,
                "SMS Reminder",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(alarmReminder)

            val lansinohReminder = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Reminder",
                NotificationManager.IMPORTANCE_HIGH
            )

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(lansinohReminder)

            val lansinohNonSmartPump = NotificationChannel(
                NON_SMART_PUMP_CHANNEL_ID,
                "NonSmartPump",
                NotificationManager.IMPORTANCE_HIGH
            )

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(lansinohNonSmartPump)
        }
    }

}