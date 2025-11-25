package com.sanda.truckdoc.updater.data.repository

import com.sanda.truckdoc.updater.data.model.SystemUpdateInfo

/**
 * Interface for update providers.
 * Abstracts the source of updates (GitHub, Jenkins, Custom Server, etc).
 */
interface UpdateProvider {
    suspend fun checkForUpdates(): SystemUpdateInfo
    suspend fun isAppInstalled(packageName: String): Boolean
}

