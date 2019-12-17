package com.infomantri.autosms.send.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.infomantri.autosms.send.activity.AddAlarmsActivity
import com.infomantri.autosms.send.constants.AppConstant
import com.infomantri.autosms.send.util.getStringFromPreference
import com.infomantri.autosms.send.util.phoneCallToNumber

class DozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.v("DOZE_ALARM_onReceive", ">>> Doze Alarm onReceive() ... Action: ${intent?.action}")
        context?.let {
            playRingtone(context)
            vibratePhone(context)

            context.phoneCallToNumber(
                context.getStringFromPreference(AppConstant.DEFAULT_MOBILE_NO) ?: "9321045517"
            )
            sendNotification(
                context,
                System.currentTimeMillis().toInt(),
                "Phone Call Alarm Successfully Done",
                "Called to Nitin Jio for Alarm Wakeup...",
                AddAlarmsActivity::class.java
            )
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