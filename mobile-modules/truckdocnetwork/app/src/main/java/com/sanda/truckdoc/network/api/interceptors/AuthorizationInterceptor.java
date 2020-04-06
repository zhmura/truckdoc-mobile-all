package com.sanda.truckdoc.network.api.interceptors;

import android.util.Base64;

import com.sanda.truckdoc.network.api.UserKey;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by k.natallie on 04.11.2016.
 */

public class AuthorizationInterceptor implements Interceptor {
    @NotNull
    private UserKey userKey;

    public AuthorizationInterceptor(@NotNull UserKey userKey) {
        this.userKey = userKey;
    }

    private String encodeCredentialsForBasicAuthorization() {
        final String credentials = userKey.getLogin() + ":" + userKey.getSecret();
        return "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        String authorizationValue = encodeCredentialsForBasicAuthorization();
        Request authorizedRequest = originalRequest.newBuilder()
                .removeHeader("Authorization")
                .addHeader("Authorization", authorizationValue)
                .build();
        return chain.proceed(authorizedRequest);
    }
}
