package com.sanda.truckdoc.updater.data.model

import com.google.gson.annotations.SerializedName

data class JenkinsBuild(
    @SerializedName("number")
    val buildNumber: Int,
    
    @SerializedName("url")
    val buildUrl: String,
    
    @SerializedName("timestamp")
    val timestamp: Long,
    
    @SerializedName("result")
    val result: String?,
    
    @SerializedName("artifacts")
    val artifacts: List<BuildArtifact>?,
    
    @SerializedName("building")
    val isBuilding: Boolean
)

data class BuildArtifact(
    @SerializedName("fileName")
    val fileName: String,
    
    @SerializedName("relativePath")
    val relativePath: String,
    
    @SerializedName("size")
    val size: Long
)

data class AppVersion(
    val versionName: String,
    val versionCode: Int,
    val buildNumber: Int,
    val downloadUrl: String,
    val releaseNotes: String?,
    val isStable: Boolean
)

data class UpdateInfo(
    val currentVersion: AppVersion,
    val latestVersion: AppVersion?,
    val updateAvailable: Boolean,
    val lastCheckTime: Long
) 