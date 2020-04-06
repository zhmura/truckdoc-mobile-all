package com.sanda.truckdoc.network.api;

import com.sanda.truckdoc.network.AuthorizedBackend;
import com.sanda.truckdoc.network.UserScope;
import com.sanda.truckdoc.network.api.interceptors.AuthorizationInterceptor;

import org.jetbrains.annotations.NotNull;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@Module
public class AuthorizedNetworkModule {
    @NotNull
    private final UserKey userKey;

    public AuthorizedNetworkModule(@NotNull UserKey userKey) {
        this.userKey = userKey;
    }

    @Provides
    @UserScope
    @Named("auth")
    OkHttpClient provideOkHttpClient2(OkHttpClient baseClient, AuthorizationInterceptor apiRequestInterceptor) {
        OkHttpClient.Builder builder = baseClient.newBuilder();
        builder.interceptors().add(0, apiRequestInterceptor);
        return builder.build();
    }

    @Provides
    @UserScope
    AuthorizationInterceptor provideAuthInterceptor() {
        return new AuthorizationInterceptor(userKey);
    }

    @Provides
    @UserScope
    @Named("auth")
    Retrofit provideRetrofit(Retrofit.Builder builder, @Named("auth") OkHttpClient okHttpClient) {
        return builder.client(okHttpClient).build();
    }

    @Provides
    @UserScope
    @NotNull
    AuthorizedBackend provideBackend(@Named("auth") Retrofit retrofit) {
        return retrofit.create(AuthorizedBackend.class);
    }


}