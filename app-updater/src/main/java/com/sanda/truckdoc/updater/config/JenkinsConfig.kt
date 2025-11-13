package com.sanda.truckdoc.updater.config

/**
 * Local Jenkins configuration for the updater module.
 *
 * Replace the placeholder values with the real Jenkins deployment details
 * when integrating with the actual CI environment.
 */
object JenkinsConfig {
    const val JENKINS_BASE_URL: String = "https://jenkins.example.com/"
    const val JOB_NAME: String = "truckdoc-mobile-build"

    object TargetApp {
        const val PACKAGE_NAME: String = "com.sanda.truckdoc.client"
    }
}
