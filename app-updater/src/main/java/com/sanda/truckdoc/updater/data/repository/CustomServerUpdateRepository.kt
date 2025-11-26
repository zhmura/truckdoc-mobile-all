package com.sanda.truckdoc.updater.data.repository

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.google.gson.Gson
import com.sanda.truckdoc.updater.config.CustomServerConfig
import com.sanda.truckdoc.updater.data.model.AppUpdateInfo
import com.sanda.truckdoc.updater.data.model.AppVersion
import com.sanda.truckdoc.updater.data.model.JenkinsReleaseManifest
import com.sanda.truckdoc.updater.data.model.ManifestAppEntry
import com.sanda.truckdoc.updater.data.model.SystemUpdateInfo
import com.sanda.truckdoc.updater.util.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomServerUpdateRepository @Inject constructor(
    private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val preferencesManager: PreferencesManager
) : UpdateProvider {

    private val gson = Gson()

    override suspend fun checkForUpdates(): SystemUpdateInfo = withContext(Dispatchers.IO) {
        val manifestUrl = preferencesManager.customServerManifestUrl.ifBlank {
            CustomServerConfig.defaultManifestUrl()
        }

        val manifest = fetchManifest(manifestUrl)

        val clientCurrent = getCurrentAppVersion(manifest.client.packageName)
        val updaterCurrent = getCurrentAppVersion(manifest.updater.packageName)

        SystemUpdateInfo(
            clientAppUpdate = mapEntryToUpdate(manifest.client, clientCurrent),
            updaterAppUpdate = mapEntryToUpdate(manifest.updater, updaterCurrent),
            lastCheckTime = System.currentTimeMillis()
        )
    }

    override suspend fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun fetchManifest(url: String): JenkinsReleaseManifest {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        okHttpClient.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) {
                throw UpdateException("Custom server returned HTTP ${resp.code}")
            }

            val body = resp.body ?: throw UpdateException("Empty manifest response")

            body.charStream().use {
                return gson.fromJson(it, JenkinsReleaseManifest::class.java)
            }
        }
    }

    private fun mapEntryToUpdate(entry: ManifestAppEntry, currentVersion: AppVersion): AppUpdateInfo {
        val latestVersion = AppVersion(
            versionName = entry.versionName,
            versionCode = entry.versionCode,
            buildNumber = entry.buildNumber ?: 0,
            downloadUrl = entry.downloadUrl,
            releaseNotes = entry.releaseNotes,
            isStable = true
        )

        val updateAvailable = isVersionNewer(latestVersion, currentVersion)

        return AppUpdateInfo(
            packageName = entry.packageName,
            appName = entry.appName,
            currentVersion = currentVersion,
            latestVersion = latestVersion,
            updateAvailable = updateAvailable
        )
    }

    private fun getCurrentAppVersion(packageName: String): AppVersion {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
            AppVersion(
                versionName = packageInfo.versionName ?: "0.0.0",
                versionCode = getVersionCode(packageInfo),
                buildNumber = 0,
                downloadUrl = "",
                releaseNotes = null,
                isStable = true
            )
        } catch (e: PackageManager.NameNotFoundException) {
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

    private fun isVersionNewer(latestVersion: AppVersion, currentVersion: AppVersion): Boolean {
        if (latestVersion.versionCode != currentVersion.versionCode) {
            return latestVersion.versionCode > currentVersion.versionCode
        }
        return compareVersionNames(latestVersion.versionName, currentVersion.versionName) > 0
    }

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

    private fun getVersionCode(packageInfo: PackageInfo): Int {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode
        }
    }
}