package com.infomantri.autosms.sender.activity

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.infomantri.autosms.sender.R
import com.infomantri.autosms.sender.adapter.MessageListAdapter
import com.infomantri.autosms.sender.asynctask.BaseAsyncTask
import com.infomantri.autosms.sender.base.BaseActivity
import com.infomantri.autosms.sender.database.MessageDbRepository
import com.infomantri.autosms.sender.database.MessageRoomDatabase
import com.infomantri.autosms.sender.viewmodel.MessageViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bootom_sheet.*

class HomeActivity : BaseActivity() {

    private lateinit var mViewModel: MessageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mViewModel = ViewModelProviders.of(this).get(MessageViewModel::class.java)

        setOnClickListner()
        checkForPermissions()
        setRecyclerView()
    }

    private fun setOnClickListner() {
        val fab: View = findViewById(R.id.fabAddMessage)
        fab.setOnClickListener {
            val intent = Intent(this, AddMessages::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun setRecyclerView() {
        val adapter = MessageListAdapter(copiedText = { copiedText, timeStamp -> copyToClipBoard(copiedText, timeStamp) })
        recyclerview.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerview.layoutManager = linearLayoutManager

        mViewModel.allMessages.observe(this, Observer { list ->
            adapter.submitList(list)
        })
    }

    private fun copyToClipBoard(copiedText: String, timeStamp: Int) {
        val clipBoardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("MESSAGE", copiedText)
        clipBoardManager.setPrimaryClip(clipData)
        Toast.makeText(this,"Message copied to clipboard...", Toast.LENGTH_SHORT).show()

        showAlertDialog(deleteMsg = {delete -> deleteMessage(timeStamp)})
    }

    private fun deleteMessage(timeStamp: Int) {
        BaseAsyncTask(object : BaseAsyncTask.SendSMSFromDb{
            override fun onStarted() {
                val msgDao = MessageRoomDatabase.getDatabase(application).messageDbDao()
                val repository = MessageDbRepository(msgDao, timeStamp)
                val message = repository.messageByTimeStamp
                repository.deleteMessage(message)
                Toast.makeText(application, "$message >>> is deleted...", Toast.LENGTH_SHORT).show()
            }

            override fun onCompleted() {
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {

            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                true
            }

            R.id.action_set_alarm -> {
                val intent = Intent(this, AddAlarmActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                return true
            }
            else -> {

                return super.onOptionsItemSelected(item)
            }

        }
    }

    private fun setBootomSheet() {
        val bottomSheetLayout: LinearLayout = findViewById(R.id.bottomSheet)
        val bottomSheetBehavior: BottomSheetBehavior<LinearLayout> = BottomSheetBehavior.from(bottomSheetLayout)

        bottomSheet.setOnClickListener {
            if(bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    private val REQUEST_PERMISSION_SEND_SMS = 99
    private fun checkForPermissions() {
// Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            }
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.SEND_SMS),
                    REQUEST_PERMISSION_SEND_SMS)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.

        } else {
            // Permission has already been granted
            Log.v("REQUEST_PERMISSION_SMS", ">>> Permission has already been granted...")
        }
    }
}
