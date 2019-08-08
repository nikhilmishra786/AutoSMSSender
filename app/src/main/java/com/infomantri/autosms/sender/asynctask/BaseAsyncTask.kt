package com.infomantri.autosms.sender.asynctask

import android.os.AsyncTask

class BaseAsyncTask(private val sendSMSFromDb: SendSMSFromDb) : AsyncTask<Void, Void, String>() {

    interface SendSMSFromDb {

        fun onStarted()

        fun onCompleted()
    }

    override fun doInBackground(vararg p0: Void?): String {
        sendSMSFromDb.onStarted()

        return ""
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        sendSMSFromDb.onCompleted()
    }
}