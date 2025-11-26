package com.sanda.truckdoc.updater.config

/**
 * Default configuration values for the custom Jenkins-hosted release server.
 */
object CustomServerConfig {

    const val DEFAULT_ENABLED = true

    const val CLIENT_PACKAGE_NAME = "com.sanda.truckdoc.client.default"
    const val UPDATER_PACKAGE_NAME = "com.sanda.truckdoc.updater"

    private const val DEFAULT_MANIFEST_PATH = "artifact/release-bundle/release_manifest.json"

    fun defaultManifestUrl(): String {
        val base = JenkinsConfig.JENKINS_BASE_URL.trimEnd('/') +
            "/job/${JenkinsConfig.JOB_NAME}/lastSuccessfulBuild/"
        return base + DEFAULT_MANIFEST_PATH
    }
}

