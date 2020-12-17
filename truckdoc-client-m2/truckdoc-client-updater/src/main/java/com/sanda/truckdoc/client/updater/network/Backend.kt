package com.sanda.truckdoc.client.updater.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Created by astra on 07.06.2015.
 */
interface Backend {
    @GET("/check_update")
    suspend fun checkUpdates(@Query("applicationId") applicationId: String?,
                             @Query("applicationVersion") versionName: String?,
                             @Query("androidVersion") androidVersion: Int,
                             @Query("applicationCode") applicationCode: Int,
                             @Query("updaterVersionCode") updaterVersionCode: Int,
                             @Query("deviceId") deviceId: String?,
                             @Query("androidId") androidId: String?,
                             @Query("deviceUuid") deviceUuid: String?): UpdateResponse

    @GET
    suspend fun downloadFile(@Url url: String?): ResponseBody
}
