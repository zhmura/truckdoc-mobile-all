package com.sanda.truckdoc.network;

import com.sanda.truckdoc.client.api.RegistrationInfoPojo;
import com.sanda.truckdoc.client.api.v3.configuration.api.RegisterRequest;
import com.sanda.truckdoc.client.api.v3.configuration.api.RegisterResponse;

import java.util.Map;

import retrofit2.EasyCall;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface Backend {

    /**
     * @deprecated use {@link Backend#registerV3(RegisterRequest)}
     */
    @POST("v2/config/register")
    @FormUrlEncoded
    @Deprecated
    EasyCall<RegistrationInfoPojo> register(@FieldMap Map<String, String> params);

    @POST("v3/config/register")
    EasyCall<RegisterResponse> registerV3(@Body RegisterRequest request);
}
