package com.sanda.truckdoc.client.updater.network;

import retrofit.http.GET;
import retrofit.http.Query;
import io.reactivex.rxjava3.core.Observable;

/**
 * Created by astra on 07.06.2015.
 */
public interface Backend {

    @GET("/check_update")
    Observable<UpdateResponse> checkUpdates(@Query("applicationId") String applicationId,
                                            @Query("applicationVersion") String versionName,
                                            @Query("androidVersion") int androidVersion,
                                            @Query("applicationCode") int versionCode,
                                            @Query("updaterVersionCode") int updaterVersionCode,
                                            @Query("deviceId") String deviceId,
                                            @Query("androidId") String androidId,
                                            @Query("deviceUuid") String deviceUuid);
}
