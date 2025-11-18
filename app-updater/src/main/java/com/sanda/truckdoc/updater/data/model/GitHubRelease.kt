package com.sanda.truckdoc.updater.data.model

import com.google.gson.annotations.SerializedName

/**
 * GitHub Release API response models
 */
data class GitHubRelease(
    @SerializedName("tag_name")
    val tagName: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("body")
    val body: String?,
    
    @SerializedName("draft")
    val draft: Boolean,
    
    @SerializedName("prerelease")
    val prerelease: Boolean,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("published_at")
    val publishedAt: String?,
    
    @SerializedName("assets")
    val assets: List<GitHubAsset>
)

data class GitHubAsset(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("browser_download_url")
    val downloadUrl: String,
    
    @SerializedName("size")
    val size: Long,
    
    @SerializedName("content_type")
    val contentType: String
)

/**
 * Represents update info for a specific app
 */
data class AppUpdateInfo(
    val packageName: String,
    val appName: String,
    val currentVersion: AppVersion,
    val latestVersion: AppVersion?,
    val updateAvailable: Boolean
)

/**
 * Combined update info for all target apps
 */
data class SystemUpdateInfo(
    val clientAppUpdate: AppUpdateInfo,
    val updaterAppUpdate: AppUpdateInfo,
    val lastCheckTime: Long
) {
    fun hasAnyUpdate(): Boolean = 
        clientAppUpdate.updateAvailable || updaterAppUpdate.updateAvailable
}


