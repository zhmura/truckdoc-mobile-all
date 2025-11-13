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
        super.onCreate()
        if (BuildConfig.DEBUG) {
            turnOnStrictMode()
        }
        permitDiskReads()
        Timber.plant(FileLoggingTree())
    }

    private fun turnOnStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )
    }

    private fun permitDiskReads() {
        val oldThreadPolicy = StrictMode.getThreadPolicy()
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder(oldThreadPolicy)
                .permitDiskWrites()
                .permitDiskReads()
                .build()
        )
        StrictMode.setThreadPolicy(oldThreadPolicy)
    }

    override fun onTerminate() {
        GetNewMessagesAlarmManager.cancelGetMessagesAlarm(applicationContext)
        super.onTerminate()
    }
} 