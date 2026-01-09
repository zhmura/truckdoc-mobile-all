package com.sanda.truckdoc.network.api;

import com.sanda.truckdoc.network.AuthorizedBackend;
import com.sanda.truckdoc.network.api.interceptors.AuthorizationInterceptor;

import org.jetbrains.annotations.NotNull;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class AuthorizedNetworkFactory {
    private static AuthorizedBackend authorizedBackend;
    private static OkHttpClient authorizedClient;
    private static Retrofit authorizedRetrofit;

    public static AuthorizedBackend getAuthorizedBackend(OkHttpClient baseClient, Retrofit.Builder retrofitBuilder, UserKey userKey) {
        if (authorizedBackend == null) {
            authorizedBackend = getAuthorizedRetrofit(baseClient, retrofitBuilder, userKey)
                    .create(AuthorizedBackend.class);
        }
        return authorizedBackend;
    }

    public static OkHttpClient getAuthorizedClient(OkHttpClient baseClient, UserKey userKey) {
        if (authorizedClient == null) {
            OkHttpClient.Builder builder = baseClient.newBuilder();
            builder.interceptors().add(0, getAuthInterceptor(userKey));
            authorizedClient = builder.build();
        }
        return authorizedClient;
    }

    public static Retrofit getAuthorizedRetrofit(OkHttpClient baseClient, Retrofit.Builder retrofitBuilder, UserKey userKey) {
        if (authorizedRetrofit == null) {
            authorizedRetrofit = retrofitBuilder
                    .client(getAuthorizedClient(baseClient, userKey))
                    .build();
        }
        return authorizedRetrofit;
    }

    private static AuthorizationInterceptor getAuthInterceptor(UserKey userKey) {
        return new AuthorizationInterceptor(userKey);
    }
} 