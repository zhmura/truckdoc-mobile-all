package com.sanda.truckdoc.updater.data.model

import com.google.gson.annotations.SerializedName

data class JenkinsReleaseManifest(
    @SerializedName("releaseTag")
    val releaseTag: String,
    @SerializedName("buildNumber")
    val buildNumber: Int,
    @SerializedName("generatedAt")
    val generatedAt: String,
    @SerializedName("source")
    val source: ManifestSource?,
    @SerializedName("client")
    val client: ManifestAppEntry,
    @SerializedName("updater")
    val updater: ManifestAppEntry
)

data class ManifestSource(
    @SerializedName("jobName")
    val jobName: String?,
    @SerializedName("buildUrl")
    val buildUrl: String?
)

data class ManifestAppEntry(
    @SerializedName("packageName")
    val packageName: String,
    @SerializedName("appName")
    val appName: String,
    @SerializedName("versionName")
    val versionName: String,
    @SerializedName("versionCode")
    val versionCode: Int,
    @SerializedName("buildNumber")
    val buildNumber: Int?,
    @SerializedName("downloadUrl")
    val downloadUrl: String,
    @SerializedName("fileName")
    val fileName: String?,
    @SerializedName("fileSizeBytes")
    val fileSizeBytes: Long?,
    @SerializedName("sha256")
    val sha256: String?,
    @SerializedName("releaseNotes")
    val releaseNotes: String?
)

