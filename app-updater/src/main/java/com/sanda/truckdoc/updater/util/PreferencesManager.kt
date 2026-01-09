package com.sanda.truckdoc.updater.util

import android.content.Context
import android.content.SharedPreferences
import com.sanda.truckdoc.updater.config.CustomServerConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityUtils: SecurityUtils
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
        
        // Admin settings
        private const val KEY_CUSTOM_REPO_OWNER = "custom_repo_owner"
        private const val KEY_CUSTOM_REPO_NAME = "custom_repo_name"
        private const val KEY_USE_CUSTOM_SERVER = "use_custom_server"
        private const val KEY_CUSTOM_SERVER_MANIFEST_URL = "custom_server_manifest_url"
        private const val KEY_ADMIN_PASSWORD_HASH = "admin_password_hash"
        
        // Default admin password (should be changed)
        private const val DEFAULT_ADMIN_PASSWORD = "admin123"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    init {
        if (!prefs.contains(KEY_CUSTOM_SERVER_MANIFEST_URL)) {
            prefs.edit()
                .putString(KEY_CUSTOM_SERVER_MANIFEST_URL, CustomServerConfig.defaultManifestUrl())
                .apply()
        }
        if (!prefs.contains(KEY_USE_CUSTOM_SERVER)) {
            prefs.edit()
                .putBoolean(KEY_USE_CUSTOM_SERVER, CustomServerConfig.DEFAULT_ENABLED)
                .apply()
        }
    }
    
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
    
    // Admin settings for custom GitHub repo
    var customRepoOwner: String
        get() = prefs.getString(KEY_CUSTOM_REPO_OWNER, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_REPO_OWNER, value).apply()
    
    var customRepoName: String
        get() = prefs.getString(KEY_CUSTOM_REPO_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_REPO_NAME, value).apply()

    var isCustomServerEnabled: Boolean
        get() = prefs.getBoolean(KEY_USE_CUSTOM_SERVER, CustomServerConfig.DEFAULT_ENABLED)
        set(value) = prefs.edit().putBoolean(KEY_USE_CUSTOM_SERVER, value).apply()

    var customServerManifestUrl: String
        get() = prefs.getString(KEY_CUSTOM_SERVER_MANIFEST_URL, CustomServerConfig.defaultManifestUrl())
            ?: CustomServerConfig.defaultManifestUrl()
        set(value) = prefs.edit()
            .putString(
                KEY_CUSTOM_SERVER_MANIFEST_URL,
                value.ifBlank { CustomServerConfig.defaultManifestUrl() }
            )
            .apply()
    
    private var adminPasswordHash: String
        get() = prefs.getString(KEY_ADMIN_PASSWORD_HASH, legacyHashPassword(DEFAULT_ADMIN_PASSWORD)) ?: legacyHashPassword(DEFAULT_ADMIN_PASSWORD)
        set(value) = prefs.edit().putString(KEY_ADMIN_PASSWORD_HASH, value).apply()
    
    // Utility methods
    
    /**
     * Verify admin password with migration support
     */
    fun verifyAdminPassword(password: String): Boolean {
        val storedHash = adminPasswordHash
        
        // 1. Try new HMAC hash (Secure)
        val hmacHash = securityUtils.hashPassword(password)
        if (hmacHash == storedHash) return true
        
        // 2. Fallback: Try legacy SHA-256 hash
        val legacyHash = legacyHashPassword(password)
        if (legacyHash == storedHash) {
            // Migration: Upgrade storage to HMAC
            adminPasswordHash = hmacHash
            return true
        }
        
        return false
    }
    
    /**
     * Set new admin password using HMAC
     */
    fun setAdminPassword(newPassword: String) {
        adminPasswordHash = securityUtils.hashPassword(newPassword)
    }
    
    /**
     * Get the GitHub repo configuration
     * Returns custom repo if set, otherwise default from GitHubConfig
     */
    fun getGitHubRepoConfig(): Pair<String, String> {
        val owner = customRepoOwner.ifEmpty { 
            com.sanda.truckdoc.updater.config.GitHubConfig.REPO_OWNER 
        }
        val name = customRepoName.ifEmpty { 
            com.sanda.truckdoc.updater.config.GitHubConfig.REPO_NAME 
        }
        return Pair(owner, name)
    }
    
    /**
     * Check if custom repo is configured
     */
    fun hasCustomRepo(): Boolean {
        return customRepoOwner.isNotEmpty() && customRepoName.isNotEmpty()
    }
    
    /**
     * Clear custom repo configuration
     */
    fun clearCustomRepo() {
        customRepoOwner = ""
        customRepoName = ""
    }
    
    /**
     * Legacy SHA-256 hashing (Deprecated)
     * Kept for migration purposes only
     */
    private fun legacyHashPassword(password: String): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            password 
        }
    }
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
 