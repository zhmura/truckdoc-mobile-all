package com.sanda.truckdoc.client.service.network;

import android.content.Context;

import com.sanda.truckdoc.client.HiltEntryPoint;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.network.AuthorizedBackend;
import com.sanda.truckdoc.network.api.UserKey;
import com.sanda.truckdoc.network.api.interceptors.AuthorizationInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.EasyCallAdapterFactory;
import retrofit2.Retrofit;
import retrofit2.RxErrorHandlingCallAdapterFactory;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Creates an {@link AuthorizedBackend} instance using runtime credentials.
 *
 * <p>We can't rely on a static DI module for credentials because {@link UserKey} is loaded at runtime.
 */
public final class AuthorizedBackendFactory {
    private AuthorizedBackendFactory() {}

    public static AuthorizedBackend create(Context context, UserKey userKey) {
        HiltEntryPoint entryPoint = TruckDocApp.getEntryPoint(context);

        OkHttpClient baseClient = entryPoint.okHttpClient();
        OkHttpClient authorizedClient = baseClient.newBuilder()
                .addInterceptor(0, new AuthorizationInterceptor(userKey))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(com.sanda.truckdoc.client.BuildConfig.API_BASE_URL)
                .client(authorizedClient)
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(EasyCallAdapterFactory.create())
                .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();

        return retrofit.create(AuthorizedBackend.class);
    }
}

