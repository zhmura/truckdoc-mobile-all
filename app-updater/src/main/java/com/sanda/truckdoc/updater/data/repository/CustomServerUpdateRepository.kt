package com.sanda.truckdoc.updater.data.repository

import android.content.Context
import com.sanda.truckdoc.updater.data.model.AppUpdateInfo
import com.sanda.truckdoc.updater.data.model.AppVersion
import com.sanda.truckdoc.updater.data.model.SystemUpdateInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UpdateProvider for a custom hosting server (e.g. internal Jenkins artifact server).
 * This is a placeholder for future implementation when moving away from GitHub.
 */
@Singleton
class CustomServerUpdateRepository @Inject constructor(
    private val context: Context
) : UpdateProvider {

    override suspend fun checkForUpdates(): SystemUpdateInfo {
        // TODO: Implement API call to custom server
        // val release = customApiService.getLatestRelease()
        
        return SystemUpdateInfo(
            clientAppUpdate = createEmptyUpdate("com.sanda.truckdoc.client.default", "TruckDoc Client"),
            updaterAppUpdate = createEmptyUpdate("com.sanda.truckdoc.updater", "TruckDoc Updater"),
            lastCheckTime = System.currentTimeMillis()
        )
    }

    override suspend fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun createEmptyUpdate(pkg: String, name: String): AppUpdateInfo {
        return AppUpdateInfo(
            packageName = pkg,
            appName = name,
            currentVersion = AppVersion("0.0.0", 0, 0, "", null, true),
            latestVersion = null,
            updateAvailable = false
        )
    }
}

