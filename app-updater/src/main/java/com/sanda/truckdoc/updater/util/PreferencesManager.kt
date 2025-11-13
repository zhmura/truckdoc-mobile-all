package com.sanda.truckdoc.updater.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val PREF_NAME = "app_updater_preferences"
        private const val KEY_AUTO_CHECK_ENABLED = "auto_check_enabled"
        private const val KEY_CHECK_INTERVAL_HOURS = "check_interval_hours"
        private const val KEY_LAST_CHECK_TIME = "last_check_time"
        private const val KEY_LAST_UPDATE_TIME = "last_update_time"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_AUTO_DOWNLOAD = "auto_download"
        private const val KEY_WIFI_ONLY = "wifi_only"
        private const val KEY_LAST_KNOWN_VERSION = "last_known_version"
        private const val KEY_LAST_KNOWN_VERSION_CODE = "last_known_version_code"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    // Auto-check settings
    var isAutoCheckEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_CHECK_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_CHECK_ENABLED, value).apply()
    
    var checkIntervalHours: Long
        get() = prefs.getLong(KEY_CHECK_INTERVAL_HOURS, 6L)
        set(value) = prefs.edit().putLong(KEY_CHECK_INTERVAL_HOURS, value).apply()
    
    // Timestamps
    var lastCheckTime: Long
        get() = prefs.getLong(KEY_LAST_CHECK_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_CHECK_TIME, value).apply()
    
    var lastUpdateTime: Long
        get() = prefs.getLong(KEY_LAST_UPDATE_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_UPDATE_TIME, value).apply()
    
    // Notification settings
    var isNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()
    
    // Download settings
    var isAutoDownloadEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_DOWNLOAD, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_DOWNLOAD, value).apply()
    
    var isWifiOnly: Boolean
        get() = prefs.getBoolean(KEY_WIFI_ONLY, true)
        set(value) = prefs.edit().putBoolean(KEY_WIFI_ONLY, value).apply()
    
    // Version tracking
    var lastKnownVersion: String
        get() = prefs.getString(KEY_LAST_KNOWN_VERSION, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_KNOWN_VERSION, value).apply()
    
    var lastKnownVersionCode: Int
        get() = prefs.getInt(KEY_LAST_KNOWN_VERSION_CODE, 0)
        set(value) = prefs.edit().putInt(KEY_LAST_KNOWN_VERSION_CODE, value).apply()
    
    // Utility methods
    fun shouldCheckForUpdates(): Boolean {
        if (!isAutoCheckEnabled) return false
        
        val now = System.currentTimeMillis()
        val lastCheck = lastCheckTime
        val intervalMs = checkIntervalHours * 60 * 60 * 1000
        
        return (now - lastCheck) >= intervalMs
    }
    
    fun updateLastCheckTime() {
        lastCheckTime = System.currentTimeMillis()
    }
    
    fun updateLastUpdateTime() {
        lastUpdateTime = System.currentTimeMillis()
    }
    
    fun clearAllPreferences() {
        prefs.edit().clear().apply()
    }
    
    fun getFormattedLastCheckTime(): String {
        val timestamp = lastCheckTime
        if (timestamp == 0L) return "Never"
        
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
            else -> "${diff / (24 * 60 * 60 * 1000)} days ago"
        }
    }
} 
 