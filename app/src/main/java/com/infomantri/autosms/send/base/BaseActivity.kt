package com.infomantri.autosms.send.base

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.google.android.material.snackbar.Snackbar
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.activity.AddAlarmsActivity
import com.infomantri.autosms.send.activity.HomeActivity
import com.infomantri.autosms.send.activity.SettingsActivity
import com.infomantri.autosms.send.asynctask.BaseAsyncTask
import com.infomantri.autosms.send.database.CallDetails
import com.infomantri.autosms.send.database.MessageDbRepository
import com.infomantri.autosms.send.database.MessageRoomDatabase
import com.infomantri.autosms.send.receiver.DeliverReceiver
import com.infomantri.autosms.send.receiver.SentReceiver
import com.infomantri.autosms.send.viewmodel.MessageViewModel
import kotlinx.android.synthetic.main.custom_toolbar.*
import java.util.*
import kotlin.collections.ArrayList

open class BaseActivity : AppCompatActivity() {

    val IS_DEFAULT_NO = "IS_DEFAULT_NO"
    val DEFAULT_MOBILE_NO = "DEFAULT_MOBILE_NO"

    companion object {
        var MESSAGE_SPLIT_COUNT = 0
        var SENT_MESSAGE_COUNT = 0
        var DELIVERED_MESSAGE_COUNT = 0
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState)
    }

    fun getSharedPreference(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun Toolbar.setToolbar(
        showBackNav: Boolean = true,
        titleColor: Int = R.color.title,
        centerTitle: String? = null,
        bgColor: Int = R.color.red
    ) {
        setSupportActionBar(this)

        toolIvSettings.visibility = if (showBackNav) View.GONE else View.VISIBLE
        supportActionBar?.apply {
            title = ""
//            setDisplayHomeAsUpEnabled(showBackNav)
        }

        if (titleColor == R.color.white) {
            toolIvSettings.setColorFilter(ContextCompat.getColor(context, R.color.white))
            toolIvAddAlarm.setColorFilter(ContextCompat.getColor(context, R.color.white))

            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_tool_back_white)
        } else {
            toolIvAddAlarm.setColorFilter(ContextCompat.getColor(context, R.color.orange))
            toolIvSettings.setColorFilter(ContextCompat.getColor(context, R.color.lightBlue))
        }

        centerTitle?.let {
            toolTvTitle?.text = it
            toolTvTitle?.setTextColor(
                ContextCompat.getColor(
                    this@BaseActivity,
                    titleColor
                )
            )
        }

        toolbar.setBackgroundColor(ContextCompat.getColor(this@BaseActivity, bgColor))
    }

    fun String.showSnackbar(restoreData: () -> Unit) {
        val mSnackBar : Snackbar = Snackbar.make(toolbar, this, Snackbar.LENGTH_LONG)
        mSnackBar.setAction("Undo", object : View.OnClickListener {
            override fun onClick(p0: View?) {
                mSnackBar.dismiss()
                restoreData()
            }
        })
        mSnackBar.show()
        mSnackBar.setActionTextColor(ContextCompat.getColor(applicationContext,R.color.orange))
    }

    fun showAlertDialog(deleteMsg: (Boolean) -> Unit) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Delete Message")
        //set message for alert dialog
        builder.setMessage("Deleting message will remove from list")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            deleteMsg(true)
            Log.v("DIALOG", ">>> Inside Alert Dialog >>><<< Yes")
//            Toast.makeText(applicationContext, "clicked yes", Toast.LENGTH_LONG).show()
        }
//        //performing cancel action
//        builder.setNeutralButton("Cancel") { dialogInterface, which ->
////            Toast.makeText(applicationContext, "clicked cancel\n operation cancel", Toast.LENGTH_LONG).show()
//        }
        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            //            Toast.makeText(applicationContext, "clicked No", Toast.LENGTH_LONG).show()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()
    }

    fun getCallsDetails(): ArrayList<CallDetails> {

        val callDetails = ArrayList<CallDetails>()
        val contentUri = CallLog.Calls.CONTENT_URI

        try {
            val cursor = contentResolver.query(contentUri, null, null, null, null)
            cursor?.let {
                val nameUri = cursor.getColumnIndex(CallLog.Calls.CACHED_LOOKUP_URI)
                val number = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                val duration = cursor.getColumnIndex(CallLog.Calls.DURATION)
                val date = cursor.getColumnIndex(CallLog.Calls.DATE)
                val type = cursor.getColumnIndex(CallLog.Calls.TYPE)

                if (cursor.moveToFirst()) {
                    do {
                        val callType = when (cursor.getInt(type)) {
                            CallLog.Calls.INCOMING_TYPE -> "INCOMING"
                            CallLog.Calls.OUTGOING_TYPE -> "OUTGOING"
                            CallLog.Calls.MISSED_TYPE -> "MISSED"
                            CallLog.Calls.REJECTED_TYPE -> "REJECTED"
                            else -> "NOT_DEFINED"
                        }
                        val phoneNumber = cursor.getString(number)
                        val callerNameUri = cursor.getString(nameUri)
                        val callDate = cursor.getString(date)
                        val callDayTime = Date(callDate.toLong()).toString()
                        val callDuration = cursor.getString(duration)
                        callDetails.add(
                            CallDetails(
                                getCallerName(callerNameUri),
                                phoneNumber,
                                callDuration,
                                callType,
                                callDayTime
                            )
                        )
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "User denied permission", Toast.LENGTH_SHORT).show()
        }
        return callDetails
    }

    fun getCallerName(callerNameUri: String?): String {
        return if (callerNameUri != null) {

            val cursor = contentResolver.query(Uri.parse(callerNameUri), null, null, null, null)

            var name = ""
            if ((cursor?.count ?: 0 > 0)) {
                while (cursor != null && cursor.moveToNext()) {
                    name =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                }
            }
            cursor?.close()
            return name
        } else {
            "Not a contact!"
        }
    }

}