package com.infomantri.autosms.sender

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.infomantri.autosms.sender.adapter.MessageListAdapter
import com.infomantri.autosms.sender.viewmodel.MessageViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mViewModel : MessageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mViewModel = ViewModelProviders.of(this).get(MessageViewModel::class.java)


        btAdd.setOnClickListener {
            startActivity(Intent(this, AddNewMessages::class.java))
        }

        val adapter = MessageListAdapter()
        recyclerview.adapter = adapter

        mViewModel.allMessages.observe(this, Observer {list ->
            adapter.submitList(list)
        })
    }
}
