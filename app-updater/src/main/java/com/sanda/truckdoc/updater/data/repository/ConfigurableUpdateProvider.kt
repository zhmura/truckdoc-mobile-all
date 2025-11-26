package com.sanda.truckdoc.updater.data.repository

import com.sanda.truckdoc.updater.util.PreferencesManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigurableUpdateProvider @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val gitHubUpdateRepository: GitHubUpdateRepository,
    private val customServerUpdateRepository: CustomServerUpdateRepository
) : UpdateProvider {

    private fun useCustomServer(): Boolean = preferencesManager.isCustomServerEnabled

    override suspend fun checkForUpdates() =
        if (useCustomServer()) {
            customServerUpdateRepository.checkForUpdates()
        } else {
            gitHubUpdateRepository.checkForUpdates()
        }

    override suspend fun isAppInstalled(packageName: String): Boolean =
        if (useCustomServer()) {
            customServerUpdateRepository.isAppInstalled(packageName)
        } else {
            gitHubUpdateRepository.isAppInstalled(packageName)
        }
}

