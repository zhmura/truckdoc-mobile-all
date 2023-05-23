package com.sanda.truckdoc.client.service.remote;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanda.truckdoc.client.data.model.file.FileType;
import com.sanda.truckdoc.client.service.file.CountingInputStreamEntity;
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
import timber.log.Timber;

/**
 * @author Alexei Osipov
 */
public class MessageServiceClient {

    public static final String CHECK_UPDATES_PATH = "/data/check-updates";

    public static final String MESSAGES_NEW_PATH = "/messages/new";
    public static final String CONTACT_LIST_PATH = "/config/contacts/list";
    public static final String CONTACT_CONFIRM_PATH = "/config/contacts/confirm";
    public static final String MESSAGES_CREATE_PATH = "v2/messages/create";
    public static final String MESSAGES_MARK_RECEIVED_PATH = "v2/messages/mark-received";
    public static final String FILE_BINARY_DATA_PATH = "v2/messages/attachment";
    public static final String UPLOAD_FILE_PATH = "/messages/upload-file";
    public static final String REGISTER_CLIENT_PATH = "/config/register";

    public static final String DATA_TYPE_IDS_PARAM = "dataTypeIds";

    public static final String ATTACHMENT_ID_PARAM = "attachmentId";
    public static final String MESSAGE_TEXT_PARAM = "messageText";
    public static final String MESSAGE_RECIPIENT_ID = "recipientId";
    public static final String MESSAGE_IDS_PARAM = "messageIds";
    public static final String CONTACTS_LIST_VERSION = "contacts_version";
    public static final String FILE_METADATA_IDS_PARAM = "fileMetadataIds";
    public static final String FILE_NAME_PARAM = "fileName";
    public static final String FILE_TYPE_PARAM = "fileType";
    public static final String DOC_TYPE_PARAM = "docType";
    public static final String CONVERSION_TYPE_PARAM = "conversionType";
    public static final String CONVERSION_SIDE_PARAM = "convertedByClient";
    public static final String PHONE_NUMBER_PARAM = "phoneNumber";
    public static final String IMEI_PARAM = "imei";
    public static final String ANDROID_ID_PARAM = "androidId";
    public static final String ANDROID_VERSION_PARAM = "androidVersion";
    public static final String MODEL_NAME_PARAM = "modelName";
    public static final String CLIENT_VERSION_PARAM = "clientVersion";
    public static final String REGISTRATION_TOKEN_PARAM = "registrationToken";
    public static final String APP_VERSION = "appVersion";
    public static final String GENERATED_NAME = "generatedName";

    protected static final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false);

    public static void markMessagesAsReceived(QueryContext context,
                                              List<Integer> messageIds) throws RemoteCallException {
        Map<String, ?> params = Collections.singletonMap(MESSAGE_IDS_PARAM, messageIds);
        consume(postContent(context, MESSAGES_MARK_RECEIVED_PATH, params));
    }

    public static void sendMessageByFileMetadataId(final QueryContext context,
                                                   long recipientId,
                                                   String messageText,
                                                   List<Long> fileMetadataIds) throws RemoteCallException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(MESSAGE_TEXT_PARAM, messageText);
        params.put(MESSAGE_RECIPIENT_ID, recipientId);
        params.put(FILE_METADATA_IDS_PARAM, fileMetadataIds);

        Reader r = postContent(context, MESSAGES_CREATE_PATH, params);
        consume(r);
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

    /**
     * Uploads single file without attaching it to com.sanda.truckdoc.client.ui.message.
     *
     * @param context query context
     * @param entity  local file for upload
     * @return file metadata id
     * @throws RemoteCallException
     */
    public static Long uploadFileForMessage(final QueryContext context,
                                            final CountingInputStreamEntity entity) throws RemoteCallException {
        try {
            Timber.i("File uploading started for file " + entity.getFileName());
            HashMap<String, Object> params = new HashMap<>();
            params.put(FILE_NAME_PARAM, entity.getFileName());
            params.put(FILE_TYPE_PARAM, entity.getFileType());
            if (entity.getFileType().equals(FileType.DOC)) {
                if (entity.getDocType() != null) {
                    params.put(DOC_TYPE_PARAM, entity.getDocType());
                }
                params.put(CONVERSION_TYPE_PARAM, entity.getConversionType());
                params.put(CONVERSION_SIDE_PARAM, entity.isConvertedOnClient() ? 1 : 0);
            }
            final HttpResponse resp = context.getHttpClient()
                    .execute(HttpPost.METHOD_NAME, context, UPLOAD_FILE_PATH, params, null, entity);
            try {
                checkStatus(resp, UPLOAD_FILE_PATH);
                Reader r = getResponseReader(resp);
                //return new Gson().fromJson(r, Long.class);
                Timber.i("File uploading ended for file " + entity.getFileName());
                return mapper.readValue(r, Long.class);
            } finally {
                IOUtils.closeQuietly(resp.getEntity().getContent());
            }
        } catch (Exception e) {
            Timber.e(e, "upload file for message failed");
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
