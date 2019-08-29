package com.infomantri.autosms.sender.activity

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import com.infomantri.autosms.sender.asynctask.BaseAsyncTask
import com.infomantri.autosms.sender.base.BaseActivity
import com.infomantri.autosms.sender.database.Message
import com.infomantri.autosms.sender.viewmodel.MessageViewModel
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Handler
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.infomantri.autosms.sender.R
import com.infomantri.autosms.sender.database.MessageDbRepository
import com.infomantri.autosms.sender.database.MessageRoomDatabase
import com.infomantri.autosms.sender.receiver.DeliverReceiver
import com.infomantri.autosms.sender.receiver.SentReceiver
import kotlinx.android.synthetic.main.activity_add_message.*
import java.io.Serializable


class AddMessages : BaseActivity() {

    private lateinit var mViewModel: MessageViewModel
    private var mCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_message)

        mViewModel = ViewModelProviders.of(this).get(MessageViewModel::class.java)
        setOnClickListner()
    }

    private fun setOnClickListner() {

        val fab: View = findViewById(R.id.fabSaveMessage)
        fab.setOnClickListener {
            val msg = etEnterMessage.text.toString()
            if (msg.isEmpty().not()) {
                mViewModel.insert(Message(msg, System.currentTimeMillis(), false))
            }
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        btnSendSMS.setOnClickListener {
            //            val intent = Intent(this, AddAlarmActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//            startActivity(intent)        }

            if (mCount == 3) {
                sendSMS(application)
                mCount++
            }
            else {
                mCount++
                Toast.makeText(this,"Clicked: ${mCount}", Toast.LENGTH_SHORT).show()
            }
        }


    }
}