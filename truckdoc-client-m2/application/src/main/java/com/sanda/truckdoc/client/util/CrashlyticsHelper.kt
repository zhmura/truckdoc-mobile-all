package com.sanda.truckdoc.client.util

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashlyticsHelper @Inject constructor() {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    fun logException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    fun logMessage(message: String) {
        crashlytics.log(message)
    }

    fun setUserIdentifier(userId: String) {
        crashlytics.setUserId(userId)
    }

    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Long) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Float) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Double) {
        crashlytics.setCustomKey(key, value)
    }
} 