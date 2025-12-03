package com.sanda.truckdoc.network.api.interceptors;

import android.net.TrafficStats;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Tags network sockets so StrictMode can attribute traffic to the app.
 */
public class TrafficStatsInterceptor implements Interceptor {
    private static final int TRAFFIC_TAG = 0x545244; // "TRD"

    @Override
    public Response intercept(Chain chain) throws IOException {
        TrafficStats.setThreadStatsTag(TRAFFIC_TAG);
        try {
            return chain.proceed(chain.request());
        } finally {
            TrafficStats.clearThreadStatsTag();
        }
    }
}

