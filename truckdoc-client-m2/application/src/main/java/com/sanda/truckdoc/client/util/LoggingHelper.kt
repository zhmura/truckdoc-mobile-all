package com.sanda.truckdoc.client.util

import android.app.Application
import org.slf4j.LoggerFactory
import timber.log.Timber

class LoggingHelper private constructor(
    private val application: Application
) {
    companion object {
        @Volatile
        private var instance: LoggingHelper? = null

        fun getInstance(application: Application): LoggingHelper {
            return instance ?: synchronized(this) {
                instance ?: LoggingHelper(application).also { instance = it }
            }
        }
    }

    fun initialize() {
        // Always plant debug tree for now
        Timber.plant(Timber.DebugTree())
        
        // Configure SLF4J to use a simple logger
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out")
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug")
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true")
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yyyy-MM-dd HH:mm:ss.SSS")
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "true")
        System.setProperty("org.slf4j.simpleLogger.showLogName", "true")
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true")
    }

    fun getLogger(name: String) = LoggerFactory.getLogger(name)
} 