package com.sanda.truckdoc.updater.config

/**
 * GitHub configuration for checking releases.
 * This updater checks for updates for both the main TruckDoc client app
 * and its own updater app from GitHub releases.
 */
object GitHubConfig {
    const val GITHUB_BASE_URL = "https://api.github.com/"
    const val REPO_OWNER = "zhmura"
    const val REPO_NAME = "truckdoc-mobile-all"
    
    /**
     * Target apps to check for updates
     */
    object TargetApps {
        // Main TruckDoc client app
        const val CLIENT_PACKAGE_NAME = "com.sanda.truckdoc.client.default"
        const val CLIENT_APK_PATTERN = "truckdoc-client-v"
        const val CLIENT_APK_SUFFIX = ".apk"  // No flavor suffix in release builds
        
        // Updater app itself
        const val UPDATER_PACKAGE_NAME = "com.sanda.truckdoc.updater"
        const val UPDATER_APK_PATTERN = "truckdoc-updater-v"
        const val UPDATER_APK_SUFFIX = ".apk"
    }
}


