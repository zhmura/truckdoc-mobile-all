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
import com.sanda.truckdoc.updater.util.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubUpdateRepository @Inject constructor(
    private val context: Context,
    private val gitHubApiService: GitHubApiService,
    private val preferencesManager: PreferencesManager
) : UpdateProvider {
    
    /**
     * Check for updates for both the main client app and the updater itself
     */
    override suspend fun checkForUpdates(): SystemUpdateInfo = withContext(Dispatchers.IO) {
        try {
            // Get repo configuration (custom or default)
            val (repoOwner, repoName) = preferencesManager.getGitHubRepoConfig()
            
            val latestRelease = try {
                gitHubApiService.getLatestRelease(repoOwner, repoName)
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    // No releases found - return empty update info
                    return@withContext createNoReleasesUpdateInfo(repoOwner, repoName)
                } else {
                    throw UpdateException("GitHub API error (${e.code()}): ${e.message()}", e)
                }
            }
            
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
        } catch (e: UpdateException) {
            throw e
        } catch (e: Exception) {
            throw UpdateException("Failed to check for updates from GitHub: ${e.message}", e)
        }
    }
    
    /**
     * Create update info when no releases are found
     */
    private fun createNoReleasesUpdateInfo(repoOwner: String, repoName: String): SystemUpdateInfo {
        val clientCurrent = getCurrentAppVersion(GitHubConfig.TargetApps.CLIENT_PACKAGE_NAME)
        val updaterCurrent = getCurrentAppVersion(GitHubConfig.TargetApps.UPDATER_PACKAGE_NAME)
        
        return SystemUpdateInfo(
            clientAppUpdate = AppUpdateInfo(
                packageName = GitHubConfig.TargetApps.CLIENT_PACKAGE_NAME,
                appName = "TruckDoc Client",
                currentVersion = clientCurrent,
                latestVersion = null,
                updateAvailable = false
            ),
            updaterAppUpdate = AppUpdateInfo(
                packageName = GitHubConfig.TargetApps.UPDATER_PACKAGE_NAME,
                appName = "TruckDoc Updater",
                currentVersion = updaterCurrent,
                latestVersion = null,
                updateAvailable = false
            ),
            lastCheckTime = System.currentTimeMillis()
        )
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
        
        // Extract version from filename: truckdoc-client-v1.0.3.apk -> 1.0.3
        val versionMatch = Regex("$apkPattern([\\d.]+)").find(asset.name)
        val versionName = versionMatch?.groupValues?.getOrNull(1) ?: release.tagName.removePrefix("v")
        
        // Convert version name to version code using same formula as Jenkins
        // Formula: (major * 10000) + (minor * 100) + patch
        // Examples: 1.0.0 -> 10000, 1.0.3 -> 10003, 1.2.3 -> 10203
        val versionCode = calculateVersionCode(versionName)
        
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
    override suspend fun isAppInstalled(packageName: String): Boolean {
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
     * Calculate version code from version name using Jenkins formula
     * Formula: (major * 1000000) + (minor * 10000) + patch
     * 
     * Examples:
     * - "1.0.0" -> 1000000
     * - "1.0.3" -> 1000003
     * - "1.2.3" -> 1020003
     */
    private fun calculateVersionCode(versionName: String): Int {
        val parts = versionName.split(".")
        val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
        
        return (major * 1000000) + (minor * 10000) + patch
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


