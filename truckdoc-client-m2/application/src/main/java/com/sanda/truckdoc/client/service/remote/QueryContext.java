package com.sanda.truckdoc.client.service.remote;

import com.sanda.truckdoc.client.service.HttpExecutor;
import com.sanda.truckdoc.network.api.UserKey;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexei Osipov
 */
public class QueryContext {

    /*public static final String MOBILE_API_REALM = "mobile-api";
    public static final String PARAM_REALM = "realm";*/
    private HttpExecutor httpClient;
    private UserKey userKey;
    private String apiServicePath;

    public QueryContext(@NotNull HttpExecutor httpClient, @Nullable UserKey userKey, @NotNull String apiServicePath) {
        this.httpClient = httpClient;
        this.userKey = userKey;
        this.apiServicePath = apiServicePath;
    }

    public HttpExecutor getHttpClient() {
        return httpClient;
    }

    public UserKey getUserKey() {
        return userKey;
    }

    public String getApiServicePath() {
        return apiServicePath;
    }
}
