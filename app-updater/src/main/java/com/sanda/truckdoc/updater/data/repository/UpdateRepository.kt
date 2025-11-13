package com.sanda.truckdoc.updater.data.repository

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.sanda.truckdoc.updater.config.JenkinsConfig
import com.sanda.truckdoc.updater.data.api.JenkinsApiService
import com.sanda.truckdoc.updater.data.model.AppVersion
import com.sanda.truckdoc.updater.data.model.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository @Inject constructor(
    private val context: Context,
    private val jenkinsApiService: JenkinsApiService
) {
    
    private val targetPackageName = JenkinsConfig.TargetApp.PACKAGE_NAME
    
    suspend fun checkForUpdates(): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val currentVersion = getCurrentAppVersion()
            val latestVersion = getLatestVersionFromJenkins()
            
            UpdateInfo(
                currentVersion = currentVersion,
                latestVersion = latestVersion,
                updateAvailable = latestVersion != null && latestVersion.versionCode > currentVersion.versionCode,
                lastCheckTime = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            throw UpdateException("Failed to check for updates", e)
        }
    }
    
    private fun getCurrentAppVersion(): AppVersion {
        return try {
            val packageInfo: PackageInfo = context.packageManager.getPackageInfoCompat(
                targetPackageName,
                PackageManager.GET_META_DATA
            )
            
            AppVersion(
                versionName = packageInfo.versionName ?: "Unknown",
                versionCode = packageInfo.versionCode,
                buildNumber = 0, // We don't have build number for installed app
                downloadUrl = "",
                releaseNotes = null,
                isStable = true
            )
        } catch (e: PackageManager.NameNotFoundException) {
            throw UpdateException("Target app not installed", e)
        }
    }
    
    private suspend fun getLatestVersionFromJenkins(): AppVersion? {
        return try {
            val build = jenkinsApiService.getLastSuccessfulBuild(JenkinsConfig.JOB_NAME)
            
            if (build.result == "SUCCESS" && build.artifacts != null) {
                val apkArtifact = build.artifacts.find { 
                    it.fileName.endsWith(".apk") && it.fileName.contains("release")
                }
                
                if (apkArtifact != null) {
                    val downloadUrl = "${build.buildUrl}artifact/${apkArtifact.relativePath}"
                    
                    // Extract version info from build number or artifact name
                    val versionCode = build.buildNumber
                    val versionName = "1.0.${build.buildNumber}"
                    
                    AppVersion(
                        versionName = versionName,
                        versionCode = versionCode,
                        buildNumber = build.buildNumber,
                        downloadUrl = downloadUrl,
                        releaseNotes = null,
                        isStable = true
                    )
                } else null
            } else null
        } catch (e: Exception) {
            null // Return null if we can't get latest version
        }
    }
    
    suspend fun downloadUpdate(downloadUrl: String): File = withContext(Dispatchers.IO) {
        try {
            val fileName = "truckdoc-update-${System.currentTimeMillis()}.apk"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            URL(downloadUrl).openStream().use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            
            file
        } catch (e: Exception) {
            throw UpdateException("Failed to download update", e)
        }
    }
    
    fun isAppInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfoCompat(targetPackageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}

class UpdateException(message: String, cause: Throwable? = null) : Exception(message, cause) 

private fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int): PackageInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
    } else {
        @Suppress("DEPRECATION")
        getPackageInfo(packageName, flags)
    }
}