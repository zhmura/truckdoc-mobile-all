package com.sanda.truckdoc.client.updater.work

import android.app.IntentService
import android.content.Intent
import androidx.work.WorkManager

class WorkManagerService : IntentService("WorkManagerService") {
    override fun onHandleIntent(intent: Intent?) {
        //TODO check action
        scheduleInstall(intent!!)
    }

    private fun scheduleInstall(intent: Intent) {
        val w = CheckInstallWorker.create(intent.getStringExtra("apk"))
        WorkManager.getInstance(this).enqueue(w)
    }
}
