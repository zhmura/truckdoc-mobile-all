package com.sanda.truckdoc.network.api.interceptors;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Adds header with client version to all requests.
 *
 * @author Alexei Osipov
 */
public class ClientVersionHeaderInterceptor implements Interceptor {

    private final String clientVersionName;

    public ClientVersionHeaderInterceptor(String clientVersionName) {
        this.clientVersionName = clientVersionName;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request originalRequest = chain.request();

        Request authorizedRequest = originalRequest.newBuilder()
                .removeHeader("CV")
                .addHeader("CV", clientVersionName)
                .build();
        return chain.proceed(authorizedRequest);
    }
}
