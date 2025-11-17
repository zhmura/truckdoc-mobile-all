package com.sanda.truckdoc.updater.data.repository

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.sanda.truckdoc.updater.config.GitHubConfig
import com.sanda.truckdoc.updater.data.api.GitHubApiService
import com.sanda.truckdoc.updater.data.model.AppUpdateInfo
import com.sanda.truckdoc.updater.data.model.AppVersion
import com.sanda.truckdoc.updater.data.model.GitHubAsset
import com.sanda.truckdoc.updater.data.model.GitHubRelease
import com.sanda.truckdoc.updater.data.model.SystemUpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubUpdateRepository @Inject constructor(
    private val context: Context,
    private val gitHubApiService: GitHubApiService
) {
    
    /**
     * Check for updates for both the main client app and the updater itself
     */
    suspend fun checkForUpdates(): SystemUpdateInfo = withContext(Dispatchers.IO) {
        try {
            val latestRelease = gitHubApiService.getLatestRelease(
                GitHubConfig.REPO_OWNER,
                GitHubConfig.REPO_NAME
            )
            
            val clientUpdate = checkAppUpdate(
                packageName = GitHubConfig.TargetApps.CLIENT_PACKAGE_NAME,
                appName = "TruckDoc Client",
                apkPattern = GitHubConfig.TargetApps.CLIENT_APK_PATTERN,
                release = latestRelease
            )
            
            val updaterUpdate = checkAppUpdate(
                packageName = GitHubConfig.TargetApps.UPDATER_PACKAGE_NAME,
                appName = "TruckDoc Updater",
                apkPattern = GitHubConfig.TargetApps.UPDATER_APK_PATTERN,
                release = latestRelease
            )
            
            SystemUpdateInfo(
                clientAppUpdate = clientUpdate,
                updaterAppUpdate = updaterUpdate,
                lastCheckTime = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            throw UpdateException("Failed to check for updates from GitHub", e)
        }
    }
    
    /**
     * Check for update for a specific app
     */
    private fun checkAppUpdate(
        packageName: String,
        appName: String,
        apkPattern: String,
        release: GitHubRelease
    ): AppUpdateInfo {
        val currentVersion = getCurrentAppVersion(packageName)
        val latestVersion = getLatestVersionFromRelease(release, apkPattern)
        
        return AppUpdateInfo(
            packageName = packageName,
            appName = appName,
            currentVersion = currentVersion,
            latestVersion = latestVersion,
            updateAvailable = latestVersion != null && 
                              isVersionNewer(latestVersion, currentVersion)
        )
    }
    
    /**
     * Get the currently installed version of an app
     */
    private fun getCurrentAppVersion(packageName: String): AppVersion {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_META_DATA
            )
            
            AppVersion(
                versionName = packageInfo.versionName ?: "Unknown",
                versionCode = getVersionCode(packageInfo),
                buildNumber = 0,
                downloadUrl = "",
                releaseNotes = null,
                isStable = true
            )
        } catch (e: PackageManager.NameNotFoundException) {
            // App not installed - return default version
            AppVersion(
                versionName = "0.0.0",
                versionCode = 0,
                buildNumber = 0,
                downloadUrl = "",
                releaseNotes = null,
                isStable = true
            )
        }
    }
    
    /**
     * Extract version info from GitHub release for a specific APK
     */
    private fun getLatestVersionFromRelease(
        release: GitHubRelease,
        apkPattern: String
    ): AppVersion? {
        val asset = release.assets.find { 
            it.name.startsWith(apkPattern) && it.name.endsWith(".apk")
        } ?: return null
        
        // Extract version from filename: truckdoc-client-v1.0-defaultClient.apk -> 1.0
        val versionMatch = Regex("$apkPattern([\\d.]+)").find(asset.name)
        val versionName = versionMatch?.groupValues?.getOrNull(1) ?: release.tagName.removePrefix("v")
        
        // Convert version name to version code (1.0 -> 10, 1.2.3 -> 123)
        val versionCode = versionName.split(".").joinToString("").toIntOrNull() ?: 0
        
        return AppVersion(
            versionName = versionName,
            versionCode = versionCode,
            buildNumber = 0,
            downloadUrl = asset.downloadUrl,
            releaseNotes = release.body,
            isStable = !release.prerelease
        )
    }
    
    /**
     * Check if an app is installed
     */
    fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * Compare versions to determine if latestVersion is newer
     */
    private fun isVersionNewer(latestVersion: AppVersion, currentVersion: AppVersion): Boolean {
        // First compare version codes
        if (latestVersion.versionCode != currentVersion.versionCode) {
            return latestVersion.versionCode > currentVersion.versionCode
        }
        
        // If version codes are equal, compare version names
        return compareVersionNames(latestVersion.versionName, currentVersion.versionName) > 0
    }
    
    /**
     * Compare version name strings (e.g., "1.2.3" vs "1.2.2")
     */
    private fun compareVersionNames(v1: String, v2: String): Int {
        val parts1 = v1.split(".").mapNotNull { it.toIntOrNull() }
        val parts2 = v2.split(".").mapNotNull { it.toIntOrNull() }
        
        val maxLength = maxOf(parts1.size, parts2.size)
        
        for (i in 0 until maxLength) {
            val part1 = parts1.getOrElse(i) { 0 }
            val part2 = parts2.getOrElse(i) { 0 }
            
            when {
                part1 > part2 -> return 1
                part1 < part2 -> return -1
            }
        }
        
        return 0
    }
    
    /**
     * Get version code compatible with both old and new Android APIs
     */
    private fun getVersionCode(packageInfo: PackageInfo): Int {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode
        }
    }
}

class UpdateException(message: String, cause: Throwable? = null) : Exception(message, cause)

