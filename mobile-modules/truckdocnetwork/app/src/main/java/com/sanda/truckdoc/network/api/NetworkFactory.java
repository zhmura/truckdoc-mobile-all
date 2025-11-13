package com.sanda.truckdoc.network.api;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sanda.truckdoc.network.Backend;
import com.sanda.truckdoc.network.api.interceptors.ClientVersionHeaderInterceptor;
import com.sanda.truckdoc.network.api.interceptors.HttpLoggingInterceptor;
import com.sanda.truckdoc.network.api.interceptors.UserAgentInterceptor;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.EasyCallAdapterFactory;
import retrofit2.Retrofit;
import retrofit2.RxErrorHandlingCallAdapterFactory;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class NetworkFactory {
    private static final String BASE_URL = "https://mobile.aps-solver.com/mobile-api/";
    private static OkHttpClient okHttpClient;
    private static ObjectMapper objectMapper;
    private static Retrofit retrofit;
    private static Backend backend;

    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new UserAgentInterceptor("TruckDoc/1.0"))
                .addInterceptor(new ClientVersionHeaderInterceptor("1.0"))
                .addInterceptor(loggingInterceptor)
                .build();
        }
        return okHttpClient;
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            ObjectMapper objectMapper = new ObjectMapper();
            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getOkHttpClient())
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
        }
        return retrofit;
    }

    public static Backend getBackend() {
        if (backend == null) {
            backend = getRetrofit().create(Backend.class);
        }
        return backend;
    }

    private static String buildUserAgent() {
        return "TruckDoc/" + getClientVersionName();
    }

    private static String getClientVersionName() {
        return "1.0"; // TODO: Get from BuildConfig
    }

    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.configOverride(Map.class).setInclude(
                    JsonInclude.Value.construct(
                            JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL));
        }
        return objectMapper;
    }

    public static Retrofit.Builder getRetrofitBuilder() {
        return new Retrofit.Builder()
                .baseUrl("https://mobile.aps-solver.com/mobile-api/")
                .addConverterFactory(JacksonConverterFactory.create(getObjectMapper()))
                .addCallAdapterFactory(EasyCallAdapterFactory.create())
                .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create());
    }

    protected static String buildUserAgent(Context context) {
        try {
            final PackageManager manager = context.getPackageManager();
            final PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            // Some APIs require "(gzip)" in the user-agent string.
            return info.packageName + "/" + info.versionName + " (" + info.versionCode + ") (gzip)";
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    protected static String getClientVersionName(Context context) {
        try {
            final PackageManager manager = context.getPackageManager();
            final PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
} 