package com.sanda.truckdoc.client.updater.network

import com.google.gson.annotations.SerializedName

/**
 * Created by astra on 05.07.2015.
 */
data class UpdateResponse(
        @SerializedName("updateAvailable")
        @JvmField
        val updateAvailable: Boolean = false,
        @JvmField
        @SerializedName("updateUrl")
        val url: String? = null,
        @JvmField
        @SerializedName("updateVersionCode")
        val versionCode: Int = 0
)
