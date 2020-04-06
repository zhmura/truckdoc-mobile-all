package com.sanda.truckdoc.client.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.service.CustomToast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.MessageFormat;

import javax.inject.Inject;

/**
 * Created by astra on 20.05.2015.
 */
public class NotificationHelper {

    public static final String PARAM_MSG = "com.sanda.truckdoc.client.intent.action.PARAM_MSG";
    public static final String PARAM_IS_ERROR = "com.sanda.truckdoc.client.intent.action.PARAM_IS_ERROR";

    private Context context;
    private NotificationManager notificationManager;
    private String title;

    @Inject
    public NotificationHelper(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void notifyUpload(String contentText) {
        Notification notification = createDefaultBuilder().setContentText(contentText).build();
        notificationManager.notify(1, notification);
    }

    public void startProgress() {
        Notification n = createDefaultProgressBuilder().build();
        notificationManager.notify(1, n);
    }

    public void setProgressTitle(String title) {
        this.title = title;
        notificationManager.notify(1, createDefaultProgressBuilder().setContentTitle(title).build());
    }

    public void updateProgress(int percent) {
        notificationManager.notify(1,
                createDefaultBuilder().setProgress(100, percent, false)
                        .setContentTitle(title)
                        .setContentText(percent + " %" + "/ 100 %")
                        .build());
    }

    @NotNull
    private Notification.Builder createDefaultProgressBuilder() {
        return createDefaultBuilder().setProgress(100, 0, false);
    }

    @NotNull
    private Notification.Builder createDefaultBuilder() {
        return new Notification.Builder(context).setSmallIcon(R.drawable.upload)
                .setContentTitle(context.getResources().getString(R.string.loading))
                .setContentIntent(getPendingIntent())
                .setAutoCancel(true);
    }

    private PendingIntent getPendingIntent() {
        return PendingIntent.getActivity(context, 0, new Intent(), 0);
    }

    public void uploadFile(long id, String filename, int progress, boolean autoCancel) {
        Notification n = createDefaultBuilder().setProgress(100, progress, false)
                .setContentText(filename)
                .setOngoing(true)
                .setAutoCancel(autoCancel)
                .build();
        notificationManager.notify((int) id, n);
    }

    public void uploadFinished(long id, String filename, boolean error) {
        Notification n = createDefaultBuilder().setContentText(error ? context.getString(R.string.file_upload_failed, filename) : context.getString(R.string.file_upload_finished,
                filename)).build();
        notificationManager.notify((int) id, n);
    }

    public static void showErrorMessage(Context context, int stringMessageResourceId, String methodCode) {
        CustomToast.showRed(context, MessageFormat.format(context.getResources().getString(stringMessageResourceId), methodCode));
    }

    public static String getErrorMessage(Throwable e, Context context, String methodCode) {
        if (e instanceof IOException) {
            return MessageFormat.format(context.getResources().getString(R.string.commonIOException), methodCode);
        } else {
            return MessageFormat.format(context.getResources().getString(R.string.commonException), methodCode);
        }
    }

    public static void showErrorMessage(String errorMessage, Context context) {
        CustomToast.showRed(context, errorMessage);
    }

    public static void showNotificationMessage(String message, Context context) {
        CustomToast.showBlue(context, message);
    }
}
