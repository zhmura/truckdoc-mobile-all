package com.sanda.truckdoc.network.api;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sanda.truckdoc.network.Backend;
import com.sanda.truckdoc.network.BuildConfig;
import com.sanda.truckdoc.network.api.interceptors.ClientVersionHeaderInterceptor;
import com.sanda.truckdoc.network.api.interceptors.HttpLoggingInterceptor;
import com.sanda.truckdoc.network.api.interceptors.UserAgentInterceptor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.EasyCallAdapterFactory;
import retrofit2.Retrofit;
import retrofit2.RxErrorHandlingCallAdapterFactory;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Module
public class NetworkModule {

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(UserAgentInterceptor userAgentInterceptor,
                                     HttpLoggingInterceptor httpLoggingInterceptor,
                                     ClientVersionHeaderInterceptor clientVersionHeaderInterceptor) {
        return new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(userAgentInterceptor)
                .addInterceptor(clientVersionHeaderInterceptor)
                .addInterceptor(httpLoggingInterceptor)
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
    }

    @Provides
    @Singleton
    UserAgentInterceptor provideUserAgentInterceptor(Context context) {
        return new UserAgentInterceptor(buildUserAgent(context));
    }

    @Provides
    @Singleton
    HttpLoggingInterceptor provideHttpLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.BASIC);
        return interceptor;
    }

    @Provides
    @Singleton
    ClientVersionHeaderInterceptor provideClientVersionHeaderInterceptor(Context context) {
        return new ClientVersionHeaderInterceptor(getClientVersionName(context));
    }

    @Provides
    @Singleton
    ObjectMapper provideObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        return mapper;
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(Retrofit.Builder builder, OkHttpClient okHttpClient) {
        return builder.client(okHttpClient).build();
    }

    @Provides
    @Singleton
    Retrofit.Builder provideRetrofitBuilder(ObjectMapper objectMapper) {
        // TODO: Use api_service_path!
        return new Retrofit.Builder().baseUrl("https://mobile-api.truckdoc.ru/mobile-api/")
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .addCallAdapterFactory(EasyCallAdapterFactory.create())
                .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create());
    }

    @Provides
    @NotNull Backend provideBackend(Retrofit retrofit) {
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
