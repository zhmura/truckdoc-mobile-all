package com.sanda.truckdoc.updater.util

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.sanda.truckdoc.updater.worker.UpdateCheckWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateScheduler @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val UPDATE_CHECK_WORK_NAME = "update_check_work"
    }
    
    fun schedulePeriodicUpdateCheck(intervalHours: Long = 6) {
        val updateWorkRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
            intervalHours, TimeUnit.HOURS
        ).build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UPDATE_CHECK_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            updateWorkRequest
        )
    }
    
    fun cancelPeriodicUpdateCheck() {
        WorkManager.getInstance(context).cancelUniqueWork(UPDATE_CHECK_WORK_NAME)
    }
    
    fun isPeriodicUpdateCheckScheduled(): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(UPDATE_CHECK_WORK_NAME)
            .get()
        
        return workInfos.any { !it.state.isFinished }
    }
} 