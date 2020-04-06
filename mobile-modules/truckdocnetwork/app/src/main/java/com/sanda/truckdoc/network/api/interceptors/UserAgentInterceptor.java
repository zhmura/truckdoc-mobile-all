package com.sanda.truckdoc.network.api.interceptors;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by k.natallie on 05.11.2016.
 */

public class UserAgentInterceptor implements Interceptor {
    public static final String USER_AGENT = "User-Agent";
    private final String userAgent;

    public UserAgentInterceptor(String userAgent) {
        this.userAgent = userAgent;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request requestWithUserAgent = originalRequest.newBuilder()
                .removeHeader(USER_AGENT)
                .addHeader(USER_AGENT, userAgent)
                .build();
        return chain.proceed(requestWithUserAgent);

    }
}
