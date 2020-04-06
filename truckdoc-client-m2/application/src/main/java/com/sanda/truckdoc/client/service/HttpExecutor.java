package com.sanda.truckdoc.client.service;

import com.sanda.truckdoc.client.service.file.CountingInputStreamEntity;
import com.sanda.truckdoc.client.service.remote.QueryContext;
import com.sanda.truckdoc.network.api.UserKey;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.boye.httpclientandroidlib.HttpHost;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.auth.AuthScope;
import ch.boye.httpclientandroidlib.auth.UsernamePasswordCredentials;
import ch.boye.httpclientandroidlib.client.AuthCache;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.client.methods.HttpRequestBase;
import ch.boye.httpclientandroidlib.client.params.ClientPNames;
import ch.boye.httpclientandroidlib.client.protocol.ClientContext;
import ch.boye.httpclientandroidlib.client.utils.URLEncodedUtils;
import ch.boye.httpclientandroidlib.impl.auth.BasicScheme;
import ch.boye.httpclientandroidlib.impl.client.AbstractHttpClient;
import ch.boye.httpclientandroidlib.impl.client.BasicAuthCache;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
import ch.boye.httpclientandroidlib.protocol.BasicHttpContext;
import ch.boye.httpclientandroidlib.protocol.HTTP;

/**
 * @author Alexei Osipov
 */
public class HttpExecutor {

    public static final String MIME_TYPE_APPLICATION_OCTET_STREAM = "application/octet-stream";
    private AbstractHttpClient httpClient;

    public HttpExecutor(AbstractHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpResponse execute(String methodName, QueryContext context, String path, @Nullable Map<String, ?> queryParams) throws IOException {
        return execute(methodName, context, path, queryParams, null, null);
    }

    private HttpResponse execute(String methodName, QueryContext context, String path, @Nullable Map<String, ?> queryParams, @Nullable Map<String, ?> entityParams, @Nullable File file, @Nullable CountingInputStreamEntity entity) throws IOException {
        URL urlObj = new URL(context.getApiServicePath() + path);
        //appendLog("URLobj:"+urlObj.toString());
        HttpHost targetHost = new HttpHost(urlObj.getHost(), urlObj.getPort(), urlObj.getProtocol());
        BasicHttpContext localcontext = new BasicHttpContext();

        UserKey userKey = context.getUserKey();
        //appendLog("Userkey login:"+userKey.getLogin()+'\n'+"Userkey name:"+userKey.getName()+'\n'+"Userkey secret:"+userKey.getSecret()+'\n');

        if (userKey != null) {
            httpClient.getCredentialsProvider()
                    .setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                            new UsernamePasswordCredentials(userKey.getLogin(), userKey.getSecret()));

            // Create AuthCache instance
            AuthCache authCache = new BasicAuthCache();
            // Generate BASIC scheme object and add it to the local
            // auth cache
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(targetHost, basicAuth);

            // Add AuthCache to the execution context
            localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
        }


        HttpRequestBase request;
        List<NameValuePair> queryParamPairs = paramsToNameValuePairs(queryParams);
        String paramString = queryParamPairs != null ? "?" + URLEncodedUtils.format(queryParamPairs, HTTP.UTF_8) : "";
        if (HttpGet.METHOD_NAME.equals(methodName)) {
            request = new HttpGet(urlObj.getPath() + paramString);
        } else if (HttpPost.METHOD_NAME.equals(methodName)) {
            HttpPost postRequest = new HttpPost(urlObj.getPath() + paramString);
            request = postRequest;

            List<NameValuePair> entityParamPairs = paramsToNameValuePairs(entityParams);

            if (entityParamPairs != null && file != null) {
                throw new IllegalArgumentException("Can't mix entity params and file upload");
            }

            if (entityParamPairs != null) {
                postRequest.setEntity(new UrlEncodedFormEntity(entityParamPairs, HTTP.UTF_8));
            }

            if (file != null) {
                SlicedFileEntity fileEntity = new SlicedFileEntity(file, MIME_TYPE_APPLICATION_OCTET_STREAM);
                fileEntity.setChunked(true);
                postRequest.setEntity(fileEntity);
            }

            if (entity != null) {
                postRequest.setEntity(entity);
            }
        } else {
            throw new IllegalStateException("Unknown HTTP method: " + methodName);
        }
        request.getParams().setBooleanParameter(ClientPNames.HANDLE_AUTHENTICATION, true);

        return httpClient.execute(targetHost, request, localcontext);
    }

    public HttpResponse execute(String methodName, QueryContext context, String path, @Nullable Map<String, ?> queryParams, @Nullable Map<String, ?> entityParams, @Nullable File file) throws IOException {
        return execute(methodName, context, path, queryParams, entityParams, file, null);
    }

    private List<NameValuePair> paramsToNameValuePairs(Map<String, ?> queryParams) {
        List<NameValuePair> nvps = null;
        if (queryParams != null && !queryParams.isEmpty()) {
            nvps = new ArrayList<>();
            for (String param : queryParams.keySet()) {
                Object value = queryParams.get(param);
                if (value instanceof Iterable) {
                    for (Object valuePart : (Iterable) value) {
                        nvps.add(new BasicNameValuePair(param, valuePart.toString()));
                    }
                } else {
                    nvps.add(new BasicNameValuePair(param, value.toString()));
                }
            }
        }
        return nvps;
    }

    public HttpResponse executeGet(QueryContext context, String path, @Nullable Map<String, Object> params) throws IOException {
        return execute(HttpGet.METHOD_NAME, context, path, params);
    }

    public void shutdown() {
        httpClient.getConnectionManager().shutdown();
    }

    public HttpResponse execute(String methodName, QueryContext context, String path, HashMap<String, ?> queryParams, @Nullable Map<String, ?> entityParams, CountingInputStreamEntity entity) throws IOException {
        return execute(methodName, context, path, queryParams, entityParams, null, entity);
    }
}
