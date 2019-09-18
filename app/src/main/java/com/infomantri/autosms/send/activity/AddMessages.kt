package com.infomantri.autosms.send.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.infomantri.autosms.send.base.BaseActivity
import com.infomantri.autosms.send.database.Message
import com.infomantri.autosms.send.viewmodel.MessageViewModel
import com.infomantri.autosms.send.R
import com.infomantri.autosms.send.adapter.AuthorListAdapter
import com.infomantri.autosms.send.util.getTrimmedText
import com.infomantri.autosms.send.util.removeNewLine
import kotlinx.android.synthetic.main.activity_add_message.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.custom_toolbar.*


class AddMessages : BaseActivity() {

    private lateinit var mViewModel: MessageViewModel
    private var authorName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_message)

        mViewModel = ViewModelProviders.of(this).get(MessageViewModel::class.java)
        setToolbar()
        setOnClickListener()
        setRecyclerView()
    }

    private fun setRecyclerView() {
        val adapter = AuthorListAdapter(author = { appendAuthorName(it, etEnterMessage)})
        rvAuthorList.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        linearLayoutManager.stackFromEnd = false
        rvAuthorList.layoutManager = linearLayoutManager
        adapter.submitList(listOf("Sadhguru", "Acharya Chanakya", "Swami Vivekanand", "Inspirational", "Legends"))
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
            val msg = etEnterMessage.removeNewLine()
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

    private fun appendAuthorName(authorName: String, editText: EditText) {
        val messageText = editText.getTrimmedText().plus(" - ".plus(authorName))
        editText.setText(messageText, TextView.BufferType.EDITABLE)
    }
}