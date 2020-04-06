package com.sanda.truckdoc.client.service;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.format.DateUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;

import ch.boye.httpclientandroidlib.Header;
import ch.boye.httpclientandroidlib.HeaderElement;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.conn.ClientConnectionManager;
import ch.boye.httpclientandroidlib.conn.scheme.Scheme;
import ch.boye.httpclientandroidlib.conn.scheme.SchemeRegistry;
import ch.boye.httpclientandroidlib.conn.ssl.SSLSocketFactory;
import ch.boye.httpclientandroidlib.entity.HttpEntityWrapper;
import ch.boye.httpclientandroidlib.impl.client.AbstractHttpClient;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.params.BasicHttpParams;
import ch.boye.httpclientandroidlib.params.HttpConnectionParams;
import ch.boye.httpclientandroidlib.params.HttpParams;
import ch.boye.httpclientandroidlib.params.HttpProtocolParams;

/**
 * @author Alexei Osipov
 */
public class HttpExecutorFactory {

    private static final int SECOND_IN_MILLIS = (int) DateUtils.SECOND_IN_MILLIS;
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";
    public static final int PROTOCOL_VERSION = 2;

    public static HttpExecutor getExecutor(Context context) {
        return getExecutor(context, true);
    }

    private static AbstractHttpClient getHttpClient(Context context, int protocolVersion) {
        return sslClient(getHttpClient(buildUserAgent(context), protocolVersion));
    }

    public static HttpExecutor getExecutor(Context context, boolean isSecureConnection) {
        return new HttpExecutor(isSecureConnection
                ? getHttpsClient(context)
                : getHttpClient(buildUserAgent(context), PROTOCOL_VERSION));
    }

    private static AbstractHttpClient getHttpsClient(Context context) {
        return sslClient(getHttpClient(buildUserAgent(context), PROTOCOL_VERSION));
    }

    private static AbstractHttpClient sslClient(AbstractHttpClient client) {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, null, null);
            SSLSocketFactory ssf = new SSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = client.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", ssf, 443));
            return new DefaultHttpClient(ccm, client.getParams());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Generate and return a {@link org.apache.http.client.HttpClient} configured for general use,
     * including setting an application-specific user-agent string.
     */
    private static AbstractHttpClient getHttpClient(String userAgent, int protocolVersion) {
        final HttpParams params = new BasicHttpParams();

        params.setIntParameter("protocolVersion", protocolVersion);

        // Use generous timeouts for slow mobile networks
        HttpConnectionParams.setConnectionTimeout(params, 45 * SECOND_IN_MILLIS);
        HttpConnectionParams.setSoTimeout(params, 45 * SECOND_IN_MILLIS);

        HttpConnectionParams.setSocketBufferSize(params, 8192);
        HttpProtocolParams.setUserAgent(params, userAgent);

        final DefaultHttpClient client = new DefaultHttpClient(params);

        client.addRequestInterceptor((request, context) -> {
            // Add header to accept gzip content
            if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
                request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
            }
        });

        client.addResponseInterceptor((response, context) -> {
            // Inflate any responses compressed with gzip
            final HttpEntity entity = response.getEntity();
            final Header encoding = entity.getContentEncoding();
            if (encoding != null) {
                for (HeaderElement element : encoding.getElements()) {
                    if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
                        response.setEntity(new InflatingEntity(response.getEntity()));
                        break;
                    }
                }
            }
        });

        return client;
    }

    /**
     * Build and return a user-agent string that can identify this application
     * to remote servers. Contains the package name and version code.
     */
    private static String buildUserAgent(Context context) {
        try {
            final PackageManager manager = context.getPackageManager();
            final PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);

            // Some APIs require "(gzip)" in the user-agent string.
            return info.packageName + "/" + info.versionName + " (" + info.versionCode + ") (gzip)";
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * Simple {@link org.apache.http.entity.HttpEntityWrapper} that inflates the wrapped
     * {@link org.apache.http.HttpEntity} by passing it through {@link java.util.zip.GZIPInputStream}.
     */
    private static class InflatingEntity extends HttpEntityWrapper {

        public InflatingEntity(HttpEntity wrapped) {
            super(wrapped);
        }

        @Override
        public InputStream getContent() throws IOException {
            return new GZIPInputStream(wrappedEntity.getContent());
        }

        @Override
        public long getContentLength() {
            return -1;
        }
    }
}
