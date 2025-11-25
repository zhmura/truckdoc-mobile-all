package com.sanda.truckdoc.client

import android.app.Application
import android.os.StrictMode
import android.util.Log
import com.sanda.truckdoc.client.receivers.GetNewMessagesAlarmManager
import com.sanda.truckdoc.client.util.timber.FileLoggingTree
import dagger.hilt.EntryPoints
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class TruckDocApp : Application() {
    companion object {
        private const val TAG = "TruckDocApp"
        
        fun get(context: android.content.Context): TruckDocApp {
            return context.applicationContext as TruckDocApp
        }
        
        @JvmStatic
        fun getEntryPoint(context: android.content.Context): HiltEntryPoint {
            return EntryPoints.get(context.applicationContext, HiltEntryPoint::class.java)
        }
    }

    override fun onCreate() {
        try {
            super.onCreate()
            
            Log.d(TAG, "TruckDocApp onCreate started")
            
            // Configure StrictMode
            if (BuildConfig.DEBUG) {
                // Debug: Detect everything, log it, but don't crash (yet) to avoid ANRs being hidden
                StrictMode.setThreadPolicy(
                    StrictMode.ThreadPolicy.Builder()
                        .detectAll()
                        .penaltyLog()
                        .build()
                )
                StrictMode.setVmPolicy(
                    StrictMode.VmPolicy.Builder()
                        .detectAll()
                        .penaltyLog()
                        .build()
                )
            } else {
                // Release: Permissive to avoid user crashes
                StrictMode.setThreadPolicy(
                    StrictMode.ThreadPolicy.Builder()
                        .permitDiskReads()
                        .permitDiskWrites()
                        .permitNetwork()
                        .permitCustomSlowCalls()
                        .build()
                )
                StrictMode.setVmPolicy(
                    StrictMode.VmPolicy.Builder()
                        .build()
                )
            }
            
            Log.d(TAG, "StrictMode configured")
            
            // Initialize file logging on background thread to avoid blocking main thread
            initializeLoggingAsync()
            
            Log.d(TAG, "TruckDocApp onCreate completed")
        } catch (e: Exception) {
            // Log to system log since Timber might not be initialized
            Log.e(TAG, "FATAL: TruckDocApp onCreate failed", e)
            e.printStackTrace()
            throw e
        }
    }

    private fun initializeLoggingAsync() {
        // Plant debug tree immediately for early logs
        Timber.plant(Timber.DebugTree())
        
        // Initialize file logging on background thread to avoid blocking main thread
        Thread {
            try {
                // Use app-internal storage (no permissions needed)
                Timber.plant(FileLoggingTree(applicationContext))
                Log.d(TAG, "File logging initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize file logging, continuing with debug tree only", e)
                // Debug tree already planted, so app continues working
            }
        }.start()
    }

    override fun onTerminate() {
        GetNewMessagesAlarmManager.cancelGetMessagesAlarm(applicationContext)
        super.onTerminate()
    }
} 