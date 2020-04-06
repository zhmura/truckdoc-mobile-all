package app.camera.tdoc.camera_library;

import android.content.BroadcastReceiver;

/**
 * @author Alexei Osipov
 */
public abstract class ServiceResultReceiver extends BroadcastReceiver {

    public static final String ACTION_PROCESS_START = "com.sanda.truckdoc.client.intent.action.ACTION_PROCESS_START";
    public static final String ACTION_PROCESS_FINISHED = "com.sanda.truckdoc.client.intent.action.ACTION_PROCESS_FINISHED";
    public static final String NOTIFICATION_MESSAGE = "com.sanda.truckdoc.client.intent.action.NOTIFICATION_MESSAGE";
    public static final String ACTION_PRINT_RESP = "com.sanda.truckdoc.client.intent.action.DOCS_PRINTED";
    public static final String ACTION_SENT_MESSAGES_RESP = "com.sanda.truckdoc.client.intent.action.MESSAGES_SENT";
    public static final String ACTION_SCAN_RESP = "com.sanda.truckdoc.client.intent.action.DOC_SCANNED";
    public static final String ACTION_LIST_UPDATE_START = "com.sanda.truckdoc.client.intent.action.LIST_UPDATE_START";
    public static final String ACTION_LIST_UPDATED = "com.sanda.truckdoc.client.intent.action.LIST_UPDATED";
    public static final String ACTION_GET_CONFIG_OK = "com.sanda.truckdoc.client.intent.action.GET_CONFIG_OK";
    public static final String ACTION_GET_CONFIG_ERROR = "com.sanda.truckdoc.client.intent.action.GET_CONFIG_ERROR";
    public static final String ACTION_SENT_MAINTENANCE_ERROR = "com.sanda.truckdoc.client.intent.action.SENT_MAINTENANCE_ERROR";
    public static final String ACTION_SENT_MAINTENANCE_OK = "com.sanda.truckdoc.client.intent.action.SENT_MAINTENANCE_OK";
    public static final String ACTION_UPLOAD_MNT_ATTACHMENT_OK = "com.sanda.truckdoc.client.intent.action.UPLOAD_MNT_ATTACHMENT_OK";
}
