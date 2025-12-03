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
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {
    @Provides
    @Singleton
    static OkHttpClient provideOkHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(new UserAgentInterceptor("TruckDoc/1.0"))
                .addInterceptor(new ClientVersionHeaderInterceptor("1.0"))
                .addInterceptor(loggingInterceptor)
                .build();
    }

    @Provides
    @Singleton
    static Retrofit.Builder provideRetrofitBuilder(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl("https://mobile-api.truckdoc.ru/")
                .client(okHttpClient)
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(EasyCallAdapterFactory.create())
                .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create());
    }

    @Provides
    @Singleton
    static Retrofit provideRetrofit(Retrofit.Builder builder) {
        return builder.build();
    }

    @Provides
    @Singleton
    static Backend provideBackend(Retrofit retrofit) {
        return retrofit.create(Backend.class);
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
