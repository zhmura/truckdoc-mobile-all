package com.sanda.truckdoc.updater.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sanda.truckdoc.updater.config.GitHubConfig
import com.sanda.truckdoc.updater.data.repository.GitHubUpdateRepository
import com.sanda.truckdoc.updater.service.UpdateCheckService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class UpdateCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface UpdateCheckWorkerEntryPoint {
        fun updateRepository(): GitHubUpdateRepository
    }

    override suspend fun doWork(): Result {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                UpdateCheckWorkerEntryPoint::class.java
            )
            
            val updateRepository = entryPoint.updateRepository()
            
            // Check if client app is installed
            if (!updateRepository.isAppInstalled(GitHubConfig.TargetApps.CLIENT_PACKAGE_NAME)) {
                return Result.success()
            }
            
            // Check for updates
            val systemUpdate = updateRepository.checkForUpdates()
            
            // If any update is available, start the service to handle it
            if (systemUpdate.hasAnyUpdate()) {
                UpdateCheckService.startService(applicationContext)
            }
            
            Result.success()
            
        } catch (e: Exception) {
            // Don't retry on failure, just log and return success
            // This prevents the worker from getting stuck in a retry loop
            Result.success()
        }
    }
} 