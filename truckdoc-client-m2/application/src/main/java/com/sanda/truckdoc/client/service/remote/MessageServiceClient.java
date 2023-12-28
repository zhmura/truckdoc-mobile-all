package com.sanda.truckdoc.client.service.remote;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanda.truckdoc.client.service.remote.exceptions.CommunicationException;
import com.sanda.truckdoc.client.service.remote.exceptions.RemoteCallException;
import com.sanda.truckdoc.client.service.remote.exceptions.ServiceUnavailableException;
import com.sanda.truckdoc.client.util.commons.IOUtils;

import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.HttpStatus;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;

/**
 * @author Alexei Osipov
 */
public class MessageServiceClient {

    public static final String MESSAGES_MARK_RECEIVED_PATH = "v2/messages/mark-received";
    public static final String FILE_BINARY_DATA_PATH = "v2/messages/attachment";

    public static final String DATA_TYPE_IDS_PARAM = "dataTypeIds";

    public static final String ATTACHMENT_ID_PARAM = "attachmentId";
    public static final String MESSAGE_RECIPIENT_ID = "recipientId";
    public static final String MESSAGE_IDS_PARAM = "messageIds";
    public static final String CONTACTS_LIST_VERSION = "contacts_version";
    public static final String CONVERSION_TYPE_PARAM = "conversionType";
    public static final String CONVERSION_SIDE_PARAM = "convertedByClient";

    protected static final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false);

    public static void markMessagesAsReceived(QueryContext context,
                                              List<Integer> messageIds) throws RemoteCallException {
        Map<String, ?> params = Collections.singletonMap(MESSAGE_IDS_PARAM, messageIds);
        consume(postContent(context, MESSAGES_MARK_RECEIVED_PATH, params));
    }


    /**
     * Loads content for the specified attachment.
     *
     * @param context        query context
     * @param attachedFileId ID of the attachment
     * @return stream with binary data of the specified file
     * @throws RemoteCallException exception
     */
    public static InputStream getFileBinaryData(final QueryContext context,
                                                final Integer attachedFileId) throws RemoteCallException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(ATTACHMENT_ID_PARAM, attachedFileId);
        try {
            final HttpResponse resp = context.getHttpClient().executeGet(context, FILE_BINARY_DATA_PATH, params);
            checkStatus(resp, FILE_BINARY_DATA_PATH);

            return resp.getEntity().getContent();
        } catch (IOException e) {
            throw new CommunicationException("Problem reading remote response for " + FILE_BINARY_DATA_PATH, e);
        }
    }

    private static Reader postContent(final QueryContext context,
                                      final String path,
                                      @Nullable Map<String, ?> entityParams) throws RemoteCallException {
        return requestContent(HttpPost.METHOD_NAME, context, path, null, entityParams);
    }

    public static void consume(Reader reader) {
        IOUtils.closeQuietly(reader);
    }

    private static Reader requestContent(String methodName,
                                         final QueryContext context,
                                         final String path,
                                         @Nullable Map<String, ?> params,
                                         @Nullable Map<String, ?> entityParams) throws RemoteCallException {
        try {
            final HttpResponse resp = context.getHttpClient()
                    .execute(methodName, context, path, params, entityParams, null);
            checkStatus(resp, path);

            return getResponseReader(resp);
        } catch (Exception e) {
            throw new CommunicationException("Problem reading remote response for " + " for " + path, e);
        }
    }

    public static Reader getResponseReader(HttpResponse resp) throws IOException {
        final InputStream input = resp.getEntity().getContent();
        return new BufferedReader(new InputStreamReader(input));
    }

    private static void checkStatus(HttpResponse resp, String path) throws ServiceUnavailableException {
        final int status = resp.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK) {
            throw new ServiceUnavailableException(
                    "Unexpected server response " + resp.getStatusLine() + " for " + path);
        }
    }
}
