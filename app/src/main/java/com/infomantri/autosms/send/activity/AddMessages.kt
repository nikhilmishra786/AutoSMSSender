package com.infomantri.autosms.send.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.base.BaseActivity
import com.infomantri.autosms.send.database.Message
import com.infomantri.autosms.send.viewmodel.MessageViewModel
import kotlinx.android.synthetic.main.activity_add_message.*
import kotlinx.android.synthetic.main.custom_toolbar.*


class AddMessages : BaseActivity() {

    private lateinit var mViewModel: MessageViewModel
    private var mCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_message)

        mViewModel = ViewModelProviders.of(this).get(MessageViewModel::class.java)
        setToolbar()
        setOnClickListener()


    }

    private fun setToolbar() {
        toolbar.setToolbar(
            false,
            titleColor = R.color.orange,
            centerTitle = "Add Message",
            bgColor = R.color.white
        )
    }

    private fun setOnClickListener() {

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

        toolIvAddAlarm.setOnClickListener {
            val intent = Intent(this, AddAlarmsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        toolIvSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        toolIvHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }
}