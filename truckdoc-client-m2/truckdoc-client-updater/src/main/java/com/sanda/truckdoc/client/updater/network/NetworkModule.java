package com.sanda.truckdoc.client.updater.network;

import com.sanda.truckdoc.client.updater.BuildConfig;

import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by astra on 07.06.2015.
 */
@Module
public class NetworkModule {

    private static final String TCUS_URI = "http://tcus.truckdoc.ru";

    @Provides
    @Singleton
    @NotNull Backend provideBackend() {
        return new Retrofit.Builder().baseUrl(TCUS_URI)
                .client(new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor().setLevel(BuildConfig.DEBUG ?
                        HttpLoggingInterceptor.Level.BASIC : HttpLoggingInterceptor.Level.NONE))
                        .build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(Backend.class);
    }
}
