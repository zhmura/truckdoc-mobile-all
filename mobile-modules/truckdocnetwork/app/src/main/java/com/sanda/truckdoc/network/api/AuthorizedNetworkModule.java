package com.sanda.truckdoc.network.api;

import com.sanda.truckdoc.network.AuthorizedBackend;
import com.sanda.truckdoc.network.UserScope;
import com.sanda.truckdoc.network.api.interceptors.AuthorizationInterceptor;

import org.jetbrains.annotations.NotNull;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import javax.inject.Inject;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class AuthorizedNetworkModule {
    private final String userName;
    private final String userLogin;
    private final String userSecret;

    public AuthorizedNetworkModule() {
        this.userName = "";
        this.userLogin = "";
        this.userSecret = "";
    }

    public AuthorizedNetworkModule(String userName, String userLogin, String userSecret) {
        this.userName = userName;
        this.userLogin = userLogin;
        this.userSecret = userSecret;
    }

    @Provides
    @Singleton
    @Named("network")
    UserKey provideUserKey() {
        return new UserKey(userName, userLogin, userSecret);
    }

    @Provides
    @Named("auth")
    OkHttpClient provideOkHttpClient2(OkHttpClient baseClient, AuthorizationInterceptor apiRequestInterceptor) {
        OkHttpClient.Builder builder = baseClient.newBuilder();
        builder.interceptors().add(0, apiRequestInterceptor);
        return builder.build();
    }

    @Provides
    AuthorizationInterceptor provideAuthInterceptor(@Named("network") UserKey userKey) {
        return new AuthorizationInterceptor(userKey);
    }

    @Provides
    @Named("auth")
    Retrofit provideRetrofit(Retrofit.Builder builder, @Named("auth") OkHttpClient okHttpClient) {
        return builder.client(okHttpClient).build();
    }

    @Provides
    @NotNull
    AuthorizedBackend provideBackend(@Named("auth") Retrofit retrofit) {
        return retrofit.create(AuthorizedBackend.class);
    }
}