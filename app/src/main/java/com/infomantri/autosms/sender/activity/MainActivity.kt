package com.infomantri.autosms.sender.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.infomantri.autosms.sender.R
import com.infomantri.autosms.sender.adapter.MessageListAdapter
import com.infomantri.autosms.sender.viewmodel.MessageViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mViewModel: MessageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mViewModel = ViewModelProviders.of(this).get(MessageViewModel::class.java)


        val fab: View = findViewById(R.id.fabAddMessage)
        fab.setOnClickListener {
            val intent = Intent(this, AddMessages::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)        }

        val adapter = MessageListAdapter()
        recyclerview.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerview.layoutManager = linearLayoutManager

        mViewModel.allMessages.observe(this, Observer { list ->
            adapter.submitList(list)
        })
    }
}
