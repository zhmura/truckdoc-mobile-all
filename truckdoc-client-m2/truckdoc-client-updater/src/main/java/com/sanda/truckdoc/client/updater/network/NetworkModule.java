package com.sanda.truckdoc.client.updater.network;

import com.sanda.truckdoc.client.updater.BuildConfig;

import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;

/**
 * Created by astra on 07.06.2015.
 */
@Module
public class NetworkModule {

    private static final String TCUS_URI = "http://tcus.truckdoc.ru";

    @Provides
    @Singleton
    @NotNull
    Backend provideBackend() {
        return new RestAdapter.Builder().setEndpoint(TCUS_URI)
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.BASIC : RestAdapter.LogLevel.NONE)
                .build()
                .create(Backend.class);
    }
}
