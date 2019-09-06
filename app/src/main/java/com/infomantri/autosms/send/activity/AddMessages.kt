package com.infomantri.autosms.send.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.infomantri.autosms.send.base.BaseActivity
import com.infomantri.autosms.send.database.Message
import com.infomantri.autosms.send.viewmodel.MessageViewModel
import android.widget.Toast
import com.infomantri.autosms.send.R
import kotlinx.android.synthetic.main.activity_add_message.*


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