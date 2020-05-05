package com.sanda.truckdoc.network.api;

import com.sanda.truckdoc.network.AuthorizedBackend;
import com.sanda.truckdoc.network.api.interceptors.AuthorizationInterceptor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@Module
public class AuthorizedNetworkModule {

    @Provides
    @Named("auth")
    static OkHttpClient provideOkHttpClient2(OkHttpClient baseClient, AuthorizationInterceptor apiRequestInterceptor) {
        OkHttpClient.Builder builder = baseClient.newBuilder();
        builder.interceptors().add(0, apiRequestInterceptor);
        return builder.build();
    }

    @Provides
    static AuthorizationInterceptor provideAuthInterceptor(@Nullable UserKey userKey) {
        return new AuthorizationInterceptor(userKey);
    }

    @Provides
    @Named("auth")
    static Retrofit provideRetrofit(Retrofit.Builder builder, @Named("auth") OkHttpClient okHttpClient) {
        return builder.client(okHttpClient).build();
    }

    @Provides
    static @NotNull AuthorizedBackend provideBackend(@Named("auth") Retrofit retrofit) {
        return retrofit.create(AuthorizedBackend.class);
    }
}
