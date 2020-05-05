package com.sanda.truckdoc.network;

import com.sanda.truckdoc.client.api.SynchronizeRequest;
import com.sanda.truckdoc.client.api.v2.SynchronizeResponseNew;
import com.sanda.truckdoc.client.api.v3.configuration.api.UpdateRequest;
import com.sanda.truckdoc.client.api.v3.configuration.api.UpdateResponse;
import com.sanda.truckdoc.client.api.v3.sync.maintenance.api.AddMaintenanceReportRequest;
import com.sanda.truckdoc.client.api.v3.sync.routing.model.RoutePath;
import com.sanda.truckdoc.network.api.ProgressRequestBody;
import com.sanda.truckdoc.network.api.SynchronizeCheckResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.EasyCall;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by k.natallie on 31.10.2016.
 */

public interface AuthorizedBackend {
    //M2
    @POST("v2/messages/synchronize")
    EasyCall<SynchronizeResponseNew> synchronize(@Body SynchronizeRequest request);

    //M3
    @POST
    EasyCall<SynchronizeCheckResponse> synchronizeCheck(@Url String url, @Body SynchronizeRequest request);

    //M4
    @POST("v2/messages/upload-file")
    Observable<Long> uploadImage(@Body ProgressRequestBody file, @Query("fileName") String fileName);

    //M5
    @POST("v2/messages/upload-file")
    Observable<Response<Long>> uploadImage(@Body ProgressRequestBody file,
                                           @Query("fileName") String fileName,
                                           @Query("fileType") String fileType,
                                           @Query("docType") String docType,
                                           @Query("conversionType") String conversionType,
                                           @Query("convertedByClient") int convertedByClient,
                                           @Query("designationType") String type);

    //M6
    @POST("v3/config/update")
    EasyCall<UpdateResponse> update(@Body UpdateRequest updateRequest);

    //M7
    @POST("v3/maintenanceReport/addReport")
    EasyCall<Void> sendMaintenance(@Body AddMaintenanceReportRequest request);

    //M9
    @POST("v2/messages/create")
    @FormUrlEncoded
    EasyCall<Void> sendMessage(@Field("messageText") String message,
                               @Field("recipientIdType") String recipientIdType,
                               @Field("protocolVersion") int protocolVersion,
                               @Field("recipientId") long recipientId);

    //M10
    @POST("v2/messages/create")
    @FormUrlEncoded
    Observable<Response<Void>> sendMessage(@Field("recipientId") long recipientId,
                                           @Field("recipientIdType") String recipientIdType,
                                           @Field("protocolVersion") int protocolVersion,
                                           @Field("fileMetadataIds") List<Long> fileMetadataIdsAsLong);

    //M11
    @GET("v2/route/{routeId}")
    EasyCall<RoutePath> getRoute(@Path("routeId") Long routeId);

    @POST("v3/log/upload-file")
    EasyCall<Void> sendLog(@Body ProgressRequestBody file,
                           @Query("fileName") String fileName,
                           @Query("formatVersion") Integer formatVersion);

    @GET("/messages/attachment/{fileName}")
    Call<ResponseBody> downloadFile(@Path("fileName") String fileName);
}
