package com.sanda.truckdoc.updater.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.sanda.truckdoc.updater.R
import com.sanda.truckdoc.updater.data.model.UpdateInfo
import com.sanda.truckdoc.updater.ui.MainActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val CHANNEL_ID_UPDATES = "update_notifications"
        private const val CHANNEL_ID_PROGRESS = "download_progress"
        private const val NOTIFICATION_ID_UPDATE_AVAILABLE = 1001
        private const val NOTIFICATION_ID_DOWNLOAD_PROGRESS = 1002
        private const val NOTIFICATION_ID_DOWNLOAD_COMPLETE = 1003
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not required on older Android versions
        }
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            
            // Update notifications channel
            val updateChannel = NotificationChannel(
                CHANNEL_ID_UPDATES,
                "Update Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for app updates"
                enableLights(true)
                enableVibration(true)
            }
            
            // Download progress channel
            val progressChannel = NotificationChannel(
                CHANNEL_ID_PROGRESS,
                "Download Progress",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Download progress notifications"
                enableLights(false)
                enableVibration(false)
            }
            
            notificationManager.createNotificationChannels(listOf(updateChannel, progressChannel))
        }
    }
    
    @Suppress("NotificationPermission")
    fun showUpdateAvailableNotification(updateInfo: UpdateInfo) {
        if (!hasNotificationPermission()) {
            return // Skip notification if permission not granted
        }
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_UPDATES)
            .setContentTitle("Update Available")
            .setContentText("New version ${updateInfo.latestVersion?.versionName} is available")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID_UPDATE_AVAILABLE, notification)
    }
    
    @Suppress("NotificationPermission")
    fun showDownloadProgressNotification(progress: Int, message: String) {
        if (!hasNotificationPermission()) {
            return // Skip notification if permission not granted
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PROGRESS)
            .setContentTitle("Downloading Update")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID_DOWNLOAD_PROGRESS, notification)
    }
    
    @Suppress("NotificationPermission")
    fun showDownloadCompleteNotification() {
        if (!hasNotificationPermission()) {
            return // Skip notification if permission not granted
        }
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_UPDATES)
            .setContentTitle("Download Complete")
            .setContentText("Update downloaded successfully. Tap to install.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(NOTIFICATION_ID_DOWNLOAD_PROGRESS)
        notificationManager.notify(NOTIFICATION_ID_DOWNLOAD_COMPLETE, notification)
    }
    
    fun cancelDownloadProgressNotification() {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(NOTIFICATION_ID_DOWNLOAD_PROGRESS)
    }
    
    fun cancelAllNotifications() {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancelAll()
    }
} 
 