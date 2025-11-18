package com.sanda.truckdoc.updater.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sanda.truckdoc.updater.R
import com.sanda.truckdoc.updater.data.repository.GitHubUpdateRepository
import com.sanda.truckdoc.updater.util.NotificationManager as AppNotificationManager
import com.sanda.truckdoc.updater.util.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UpdateCheckService : Service() {
    
    @Inject
    lateinit var updateRepository: GitHubUpdateRepository
    
    @Inject
    lateinit var notificationManager: AppNotificationManager
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "update_checker_channel"
        
        fun startService(context: Context) {
            val intent = Intent(context, UpdateCheckService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification("Checking for updates..."))
        
        serviceScope.launch {
            try {
                // Check if we should perform the update check
                if (!preferencesManager.shouldCheckForUpdates()) {
                    stopSelf()
                    return@launch
                }
                
                val systemUpdate = updateRepository.checkForUpdates()
                
                // Update preferences with client app info
                preferencesManager.updateLastCheckTime()
                preferencesManager.lastKnownVersion = systemUpdate.clientAppUpdate.currentVersion.versionName
                preferencesManager.lastKnownVersionCode = systemUpdate.clientAppUpdate.currentVersion.versionCode
                
                if (systemUpdate.hasAnyUpdate()) {
                    // Show notification if enabled
                    if (preferencesManager.isNotificationsEnabled) {
                        // notificationManager.showUpdateAvailableNotification(systemUpdate)
                    }
                }
                
                stopSelf()
                
            } catch (e: Exception) {
                // Log error but don't show notification for errors
                stopSelf()
            }
        }
        
        return START_NOT_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                CHANNEL_ID,
                "Update Checker",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for app updates"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(message: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("TruckDoc Updater")
        .setContentText(message)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()
} 