package com.sanda.truckdoc.client

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.sanda.truckdoc.client.util.LoggingHelper

class TruckDocApplication : Application() {
    private lateinit var loggingHelper: LoggingHelper

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        loggingHelper = LoggingHelper.getInstance(this)
        loggingHelper.initialize()
    }
} 