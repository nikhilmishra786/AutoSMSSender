package com.infomantri.autosms.sender.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import com.infomantri.autosms.sender.activity.AddMessages
import com.infomantri.autosms.sender.activity.HomeActivity
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        Log.v("ALARM_onReceive", ">>> Alarm onRecive() .....")
        val reminderId = intent?.getIntExtra("reminder_id", 1000) ?: 1111
        val title = intent?.getStringExtra("reminder_title") ?: "title: "
        val timeStamp = intent?.getIntExtra("reminder_timestamp", 0) ?: "timeStamp: "
        val subTitle = "Reminder is fired at : ${SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(timeStamp)}"

        context?.let {
            Log.e("REMINDER", ">>> Reminder recevied 1 ->>>>")
            val addMessages = AddMessages()
                addMessages.sendSMS(context)
                addMessages.sendNotification(context, reminderId, title, subTitle, HomeActivity::class.java)
            playRingtone(context)
            vibratePhone(context)
            Log.e("REMINDER", ">>> Reminder recevied 3 ->>>>")
            Toast.makeText(context, "REMINDER", Toast.LENGTH_LONG).show()
            Log.e("REMINDER", ">>> Reminder recevied 4 ->>>>")
        }
    }

    private fun playRingtone(context: Context?) {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(context, uri)
        ringtone.play()
    }

    fun vibratePhone(context: Context?) {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(5000)
        }
    }

}