package com.sanda.truckdoc.updater.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sanda.truckdoc.updater.service.UpdateCheckService

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start the update check service after a delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                UpdateCheckService.startService(context)
            }, 30000) // 30 seconds delay
        }
    }
} 